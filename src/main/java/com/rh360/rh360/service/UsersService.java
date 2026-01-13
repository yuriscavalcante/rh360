package com.rh360.rh360.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.rh360.rh360.dto.UserResponse;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.repository.UsersRepository;

@Service
public class UsersService {

    private final UsersRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsersService(UsersRepository repository) {
        this.repository = repository;
    }

    public User create(User user) {
        user.setCreatedAt(LocalDateTime.now().toString());
        user.setUpdatedAt(LocalDateTime.now().toString());
        user.setStatus("active");
        user.setRole("user");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    public List<UserResponse> findAll() {
        return repository.findAll().stream().map(UserResponse::new).collect(Collectors.toList());
    }

    public User findById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public User update(UUID id, User user) {
        User existingUser = findById(id);
        if (existingUser == null) {
            throw new RuntimeException("Usuário não encontrado");
        }
        existingUser.setEmail(user.getEmail());
        existingUser.setName(user.getName());
        existingUser.setRole(user.getRole());
        existingUser.setStatus(user.getStatus());
        existingUser.setUpdatedAt(LocalDateTime.now().toString());
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
