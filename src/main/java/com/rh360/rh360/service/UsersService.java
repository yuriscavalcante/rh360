package com.rh360.rh360.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.rh360.rh360.dto.UserPermissionRequest;
import com.rh360.rh360.dto.UserResponse;
import com.rh360.rh360.entity.Permission;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.realtime.RealTimeEvent;
import com.rh360.rh360.realtime.RealTimeTopic;
import com.rh360.rh360.realtime.NoOpRealTimePublisher;
import com.rh360.rh360.realtime.RealTimePublisher;
import com.rh360.rh360.repository.PermissionRepository;
import com.rh360.rh360.repository.UsersRepository;

@Service
public class UsersService {

    private static final Logger logger = LoggerFactory.getLogger(UsersService.class);
    
    private final UsersRepository repository;
    private final PermissionRepository permissionRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final R2StorageService r2StorageService;
    private final CompreFaceService compreFaceService;
    private final RealTimePublisher realTimePublisher;

    public UsersService(UsersRepository repository, PermissionRepository permissionRepository,
                        R2StorageService r2StorageService, CompreFaceService compreFaceService) {
        this(repository, permissionRepository, r2StorageService, compreFaceService, NoOpRealTimePublisher.INSTANCE);
    }

    @Autowired
    public UsersService(UsersRepository repository, PermissionRepository permissionRepository,
                       R2StorageService r2StorageService, CompreFaceService compreFaceService,
                       RealTimePublisher realTimePublisher) {
        this.repository = repository;
        this.permissionRepository = permissionRepository;
        this.r2StorageService = r2StorageService;
        this.compreFaceService = compreFaceService;
        this.realTimePublisher = realTimePublisher != null ? realTimePublisher : NoOpRealTimePublisher.INSTANCE;
    }

    public User create(User user) {
        return create(user, null, null);
    }

    public User create(User user, MultipartFile photo) {
        return create(user, photo, null);
    }

    public User create(User user, MultipartFile photo, List<UserPermissionRequest> permissions) {
        // Verificar se o email já existe
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }
        
        user.setCreatedAt(LocalDateTime.now().toString());
        user.setUpdatedAt(LocalDateTime.now().toString());
        user.setStatus("active");
        user.setRole("user");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Salvar usuário primeiro para obter o ID
        User savedUser = repository.save(user);
        
        logger.info("Usuário {} criado com sucesso. Foto fornecida: {}", savedUser.getId(), (photo != null && !photo.isEmpty()));
        
        // Fazer upload da foto se fornecida
        if (photo != null && !photo.isEmpty()) {
            // Primeiro tentar registrar no CompreFace (não bloqueia se falhar)
            logger.info("Tentando registrar face no CompreFace para usuário {}", savedUser.getId());
            try {
                boolean faceRegistered = compreFaceService.addFace(savedUser.getId(), photo);
                if (faceRegistered) {
                    logger.info("✓ Face do usuário {} registrada com SUCESSO no CompreFace", savedUser.getId());
                } else {
                    logger.warn("✗ Aviso: Face do usuário {} NÃO foi registrada no CompreFace (retornou false)", savedUser.getId());
                }
            } catch (Exception e) {
                // Log do erro mas NÃO interrompe o fluxo - a face poderá ser registrada posteriormente
                logger.error("✗ ERRO ao registrar face do usuário {} no CompreFace (continuando mesmo assim): {}", savedUser.getId(), e.getMessage(), e);
            }
            
            // SEMPRE fazer upload para R2, independente do resultado do CompreFace
            try {
                logger.info("Fazendo upload da foto para R2 para usuário {}", savedUser.getId());
                String photoUrl = r2StorageService.uploadPhoto(photo, savedUser.getId());
                savedUser.setPhoto(photoUrl);
                savedUser = repository.save(savedUser);
                logger.info("✓ Foto do usuário {} salva no R2 com sucesso: {}", savedUser.getId(), photoUrl);
            } catch (Exception e) {
                // Este erro SIM deve interromper, pois a foto é essencial
                logger.error("✗ ERRO CRÍTICO ao fazer upload da foto para R2: {}", e.getMessage(), e);
                throw new RuntimeException("Erro ao fazer upload da foto para R2: " + e.getMessage(), e);
            }
        }
        
        // Processar permissões se fornecidas
        if (permissions != null && !permissions.isEmpty()) {
            processPermissions(savedUser, permissions);
        }
        
        // Recarregar o usuário com as permissões
        User result = repository.findById(savedUser.getId()).orElse(savedUser);
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.USERS, "refresh", null));
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.USERS_ME, "refresh", result.getId().toString()));
        return result;
    }

    public Page<UserResponse> findAll(Pageable pageable) {
        return findAll(pageable, null);
    }

    public Page<UserResponse> findAll(Pageable pageable, String search) {
        Page<User> userPage;
        if (search != null && !search.trim().isEmpty()) {
            userPage = repository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            userPage = repository.findAll(pageable);
        }
        List<UserResponse> userResponses = userPage.getContent().stream()
            .map(UserResponse::new)
            .collect(Collectors.toList());
        return new PageImpl<>(userResponses, pageable, userPage.getTotalElements());
    }

    public User findById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public User update(UUID id, User user) {
        return update(id, user, null, null);
    }

    public User update(UUID id, User user, MultipartFile photo) {
        return update(id, user, photo, null);
    }

    public User update(UUID id, User user, MultipartFile photo, List<UserPermissionRequest> permissions) {
        User existingUser = findById(id);
        if (existingUser == null) {
            throw new RuntimeException("Usuário não encontrado");
        }
        
        // Verificar se o email já está sendo usado por outro usuário
        if (!existingUser.getEmail().equals(user.getEmail())) {
            if (repository.findByEmail(user.getEmail()).isPresent()) {
                throw new RuntimeException("Email já cadastrado");
            }
        }
        
        existingUser.setEmail(user.getEmail());
        existingUser.setName(user.getName());
        existingUser.setRole(user.getRole());
        existingUser.setStatus(user.getStatus());
        existingUser.setUpdatedAt(LocalDateTime.now().toString());
        
        // Atualizar senha se fornecida
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // Fazer upload da nova foto se fornecida
        if (photo != null && !photo.isEmpty()) {
            // Primeiro tentar atualizar no CompreFace (não bloqueia se falhar)
            logger.info("Tentando atualizar face no CompreFace para usuário {}", existingUser.getId());
            try {
                boolean faceRegistered = compreFaceService.addFace(existingUser.getId(), photo);
                if (faceRegistered) {
                    logger.info("✓ Face do usuário {} atualizada com SUCESSO no CompreFace", existingUser.getId());
                } else {
                    logger.warn("✗ Aviso: Face do usuário {} NÃO foi atualizada no CompreFace (retornou false)", existingUser.getId());
                }
            } catch (Exception e) {
                // Log do erro mas NÃO interrompe o fluxo - a face poderá ser registrada posteriormente
                logger.error("✗ ERRO ao atualizar face do usuário {} no CompreFace (continuando mesmo assim): {}", existingUser.getId(), e.getMessage(), e);
            }
            
            // SEMPRE fazer upload para R2, independente do resultado do CompreFace
            try {
                logger.info("Fazendo upload da nova foto para R2 para usuário {}", existingUser.getId());
                // Deletar foto antiga se existir
                if (existingUser.getPhoto() != null && !existingUser.getPhoto().isEmpty()) {
                    try {
                        r2StorageService.deletePhoto(existingUser.getPhoto());
                        logger.info("Foto antiga do usuário {} deletada do R2", existingUser.getId());
                    } catch (Exception e) {
                        logger.warn("Aviso: Não foi possível deletar foto antiga do R2: {}", e.getMessage());
                        // Não bloqueia se não conseguir deletar
                    }
                }
                // Fazer upload da nova foto para R2
                String photoUrl = r2StorageService.uploadPhoto(photo, existingUser.getId());
                existingUser.setPhoto(photoUrl);
                logger.info("✓ Nova foto do usuário {} salva no R2 com sucesso: {}", existingUser.getId(), photoUrl);
            } catch (Exception e) {
                // Este erro SIM deve interromper, pois a foto é essencial
                logger.error("✗ ERRO CRÍTICO ao fazer upload da foto para R2: {}", e.getMessage(), e);
                throw new RuntimeException("Erro ao fazer upload da foto para R2: " + e.getMessage(), e);
            }
        }
        
        // Processar permissões se fornecidas
        if (permissions != null) {
            processPermissions(existingUser, permissions);
        }
        
        User updatedUser = repository.save(existingUser);
        
        // Recarregar o usuário com as permissões
        User result = repository.findById(updatedUser.getId()).orElse(updatedUser);
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.USERS, "refresh", null));
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.USERS_ME, "refresh", result.getId().toString()));
        return result;
    }

    public void delete(UUID id) {
        User existingUser = findById(id);
        if (existingUser == null) {
            throw new RuntimeException("Usuário não encontrado");
        }
        existingUser.setStatus("deleted");
        existingUser.setUpdatedAt(LocalDateTime.now().toString());
        repository.save(existingUser);
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.USERS, "refresh", null));
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.USERS_ME, "refresh", existingUser.getId().toString()));
    }

    /**
     * Processa as permissões de um usuário, substituindo as existentes pelas novas
     */
    private void processPermissions(User user, List<UserPermissionRequest> permissionRequests) {
        // Deletar (soft delete) todas as permissões existentes do usuário
        List<Permission> existingPermissions = permissionRepository.findByUserId(user.getId());
        for (Permission existingPermission : existingPermissions) {
            if (existingPermission.getDeletedAt() == null || existingPermission.getDeletedAt().isEmpty()) {
                existingPermission.setDeletedAt(LocalDateTime.now().toString());
                existingPermission.setUpdatedAt(LocalDateTime.now().toString());
                permissionRepository.save(existingPermission);
            }
        }
        
        // Criar novas permissões
        for (UserPermissionRequest permRequest : permissionRequests) {
            if (permRequest.getFunction() != null && !permRequest.getFunction().isEmpty()) {
                // Verificar se já existe uma permissão com esta função (mesmo que deletada)
                Permission permission = permissionRepository
                    .findByUserIdAndFunction(user.getId(), permRequest.getFunction())
                    .orElse(new Permission());
                
                // Se já existe, restaurar (remover deletedAt), senão criar nova
                if (permission.getId() != null) {
                    permission.setDeletedAt(null);
                    permission.setIsPermitted(permRequest.getIsPermitted());
                    permission.setUpdatedAt(LocalDateTime.now().toString());
                    if (permission.getCreatedAt() == null || permission.getCreatedAt().isEmpty()) {
                        permission.setCreatedAt(LocalDateTime.now().toString());
                    }
                } else {
                    permission.setUser(user);
                    permission.setFunction(permRequest.getFunction());
                    permission.setIsPermitted(permRequest.getIsPermitted());
                    permission.setCreatedAt(LocalDateTime.now().toString());
                    permission.setUpdatedAt(LocalDateTime.now().toString());
                }
                
                permissionRepository.save(permission);
                logger.info("Permissão {} processada para o usuário {}", permRequest.getFunction(), user.getId());
            }
        }
    }

}
