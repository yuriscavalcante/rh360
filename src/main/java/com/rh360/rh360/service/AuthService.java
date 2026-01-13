package com.rh360.rh360.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.rh360.rh360.dto.LoginRequest;
import com.rh360.rh360.dto.LoginResponse;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.repository.UsersRepository;

@Service
public class AuthService {

    private final UsersRepository usersRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UsersRepository usersRepository, TokenService tokenService) {
        this.usersRepository = usersRepository;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!"active".equals(user.getStatus())) {
            throw new RuntimeException("Usuário inativo");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Senha incorreta");
        }

        // Gerar token JWT
        String token = tokenService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole() != null ? user.getRole() : "USER"
        );

        // Salvar token no banco de dados
        tokenService.saveToken(token, user.getId());

        return new LoginResponse(user.getId(), token);
    }

    public void logout(String token) {
        tokenService.deactivateToken(token);
    }
}
