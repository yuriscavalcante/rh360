package com.rh360.rh360.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        user.setStatus("active");
        user.setRole("user");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public User findById(Long id) {
        return repository.findById(id).orElse(null);
    }

}
