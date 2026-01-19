package com.rh360.rh360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.rh360.rh360.dto.LoginRequest;
import com.rh360.rh360.dto.LoginResponse;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.repository.UsersRepository;

/**
 * Testes unitários para a classe AuthService.
 * 
 * Esta classe testa todos os cenários de autenticação, incluindo:
 * - Login bem-sucedido
 * - Falhas de autenticação (usuário não encontrado, senha incorreta, usuário inativo)
 * - Logout
 * 
 * @author Sistema RH360
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - AuthService")
class AuthServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private UUID userId;
    private String email;
    private String password;
    private String encodedPassword;
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Configuração inicial antes de cada teste.
     * Cria instâncias de objetos necessários para os testes.
     */
    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userId = UUID.randomUUID();
        email = "usuario@teste.com";
        password = "senha123";
        encodedPassword = passwordEncoder.encode(password);

        user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setStatus("active");
        user.setRole("user");
    }

    @Test
    @DisplayName("Deve realizar login com sucesso quando credenciais estão corretas")
    void deveRealizarLoginComSucesso() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        
        String token = "token-jwt-gerado";
        
        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(tokenService.generateToken(userId, email, "user")).thenReturn(token);
        when(tokenService.saveToken(token, userId)).thenReturn(null);

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals(token, response.getToken());
        
        verify(usersRepository, times(1)).findByEmail(email);
        verify(tokenService, times(1)).deactivateAllUserTokens(userId);
        verify(tokenService, times(1)).generateToken(userId, email, "user");
        verify(tokenService, times(1)).saveToken(token, userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("inexistente@teste.com");
        request.setPassword(password);
        
        when(usersRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(usersRepository, times(1)).findByEmail("inexistente@teste.com");
        verify(tokenService, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário estiver inativo")
    void deveLancarExcecaoQuandoUsuarioInativo() {
        // Arrange
        user.setStatus("inactive");
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        
        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Usuário inativo", exception.getMessage());
        verify(usersRepository, times(1)).findByEmail(email);
        verify(tokenService, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando senha estiver incorreta")
    void deveLancarExcecaoQuandoSenhaIncorreta() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("senhaErrada");
        
        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Email ou Senha incorreta", exception.getMessage());
        verify(usersRepository, times(1)).findByEmail(email);
        verify(tokenService, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("Deve usar role padrão 'USER' quando role do usuário for null")
    void deveUsarRolePadraoQuandoRoleNull() {
        // Arrange
        user.setRole(null);
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        
        String token = "token-jwt-gerado";
        
        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(tokenService.generateToken(userId, email, "USER")).thenReturn(token);
        when(tokenService.saveToken(token, userId)).thenReturn(null);

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        verify(tokenService, times(1)).generateToken(userId, email, "USER");
    }

    @Test
    @DisplayName("Deve realizar logout com sucesso")
    void deveRealizarLogoutComSucesso() {
        // Arrange
        String token = "token-para-invalidar";
        doNothing().when(tokenService).deactivateToken(token);

        // Act
        authService.logout(token);

        // Assert
        verify(tokenService, times(1)).deactivateToken(token);
    }
}
