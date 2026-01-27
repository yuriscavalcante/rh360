package com.rh360.rh360.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rh360.rh360.dto.PermissionResponse;
import com.rh360.rh360.entity.Permission;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.repository.PermissionRepository;
import com.rh360.rh360.repository.UsersRepository;

@Service
public class PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);
    
    private final PermissionRepository repository;
    private final UsersRepository usersRepository;

    public PermissionService(PermissionRepository repository, UsersRepository usersRepository) {
        this.repository = repository;
        this.usersRepository = usersRepository;
    }

    public Permission create(Permission permission) {
        // Verificar se o usuário existe
        User user = usersRepository.findById(permission.getUser().getId())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Verificar se já existe uma permissão com a mesma função para o mesmo usuário
        if (repository.findByUserIdAndFunction(user.getId(), permission.getFunction()).isPresent()) {
            throw new RuntimeException("Permissão com esta função já existe para este usuário");
        }
        
        permission.setUser(user);
        permission.setCreatedAt(LocalDateTime.now().toString());
        permission.setUpdatedAt(LocalDateTime.now().toString());
        
        Permission savedPermission = repository.save(permission);
        logger.info("Permissão {} criada com sucesso para o usuário {}", savedPermission.getId(), user.getId());
        
        return savedPermission;
    }

    public Page<PermissionResponse> findAll(Pageable pageable) {
        return findAll(pageable, null);
    }

    public Page<PermissionResponse> findAll(Pageable pageable, String search) {
        Page<Permission> permissionPage;
        if (search != null && !search.trim().isEmpty()) {
            permissionPage = repository.findByFunctionContainingIgnoreCase(search.trim(), pageable);
        } else {
            permissionPage = repository.findAll(pageable);
        }
        List<PermissionResponse> permissionResponses = permissionPage.getContent().stream()
            .map(PermissionResponse::new)
            .collect(Collectors.toList());
        return new PageImpl<>(permissionResponses, pageable, permissionPage.getTotalElements());
    }

    public List<PermissionResponse> findByUserId(UUID userId) {
        List<Permission> permissions = repository.findByUserId(userId);
        return permissions.stream()
            .map(PermissionResponse::new)
            .collect(Collectors.toList());
    }

    public Permission findById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public Permission update(UUID id, Permission permission) {
        Permission existingPermission = findById(id);
        if (existingPermission == null) {
            throw new RuntimeException("Permissão não encontrada");
        }
        
        // Verificar se o usuário existe (se foi alterado)
        if (permission.getUser() != null && !existingPermission.getUser().getId().equals(permission.getUser().getId())) {
            User user = usersRepository.findById(permission.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            existingPermission.setUser(user);
        }
        
        // Verificar se já existe outra permissão com a mesma função para o mesmo usuário
        if (permission.getFunction() != null && !existingPermission.getFunction().equals(permission.getFunction())) {
            UUID userId = existingPermission.getUser().getId();
            if (repository.findByUserIdAndFunction(userId, permission.getFunction()).isPresent()) {
                throw new RuntimeException("Permissão com esta função já existe para este usuário");
            }
            existingPermission.setFunction(permission.getFunction());
        }
        
        if (permission.getIsPermitted() != null) {
            existingPermission.setIsPermitted(permission.getIsPermitted());
        }
        
        existingPermission.setUpdatedAt(LocalDateTime.now().toString());
        
        Permission updatedPermission = repository.save(existingPermission);
        logger.info("Permissão {} atualizada com sucesso", updatedPermission.getId());
        
        return updatedPermission;
    }

    public void delete(UUID id) {
        Permission existingPermission = findById(id);
        if (existingPermission == null) {
            throw new RuntimeException("Permissão não encontrada");
        }
        existingPermission.setDeletedAt(LocalDateTime.now().toString());
        existingPermission.setUpdatedAt(LocalDateTime.now().toString());
        repository.save(existingPermission);
        logger.info("Permissão {} deletada com sucesso", id);
    }

}
