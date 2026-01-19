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

import com.rh360.rh360.dto.UserResponse;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.repository.UsersRepository;

@Service
public class UsersService {

    private final UsersRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final R2StorageService r2StorageService;

    public UsersService(UsersRepository repository, R2StorageService r2StorageService) {
        this.repository = repository;
        this.r2StorageService = r2StorageService;
    }

    public User create(User user) {
        return create(user, null);
    }

    public User create(User user, MultipartFile photo) {
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
        
        // Fazer upload da foto se fornecida
        if (photo != null && !photo.isEmpty()) {
            try {
                String photoUrl = r2StorageService.uploadPhoto(photo, savedUser.getId());
                savedUser.setPhoto(photoUrl);
                savedUser = repository.save(savedUser);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao fazer upload da foto: " + e.getMessage(), e);
            }
        }
        
        return savedUser;
    }

    public Page<UserResponse> findAll(Pageable pageable) {
        Page<User> userPage = repository.findAll(pageable);
        List<UserResponse> userResponses = userPage.getContent().stream()
            .map(UserResponse::new)
            .collect(Collectors.toList());
        return new PageImpl<>(userResponses, pageable, userPage.getTotalElements());
    }

    public User findById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public User update(UUID id, User user) {
        return update(id, user, null);
    }

    public User update(UUID id, User user, MultipartFile photo) {
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
            try {
                // Deletar foto antiga se existir
                if (existingUser.getPhoto() != null && !existingUser.getPhoto().isEmpty()) {
                    r2StorageService.deletePhoto(existingUser.getPhoto());
                }
                // Fazer upload da nova foto
                String photoUrl = r2StorageService.uploadPhoto(photo, existingUser.getId());
                existingUser.setPhoto(photoUrl);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao fazer upload da foto: " + e.getMessage(), e);
            }
        }
        
        return repository.save(existingUser);
    }

    public void delete(UUID id) {
        User existingUser = findById(id);
        if (existingUser == null) {
            throw new RuntimeException("Usuário não encontrado");
        }
        existingUser.setStatus("deleted");
        existingUser.setUpdatedAt(LocalDateTime.now().toString());
        repository.save(existingUser);
    }

}
