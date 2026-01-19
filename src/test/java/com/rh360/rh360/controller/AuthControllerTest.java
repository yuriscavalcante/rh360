package com.rh360.rh360.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.rh360.rh360.dto.LoginRequest;
import com.rh360.rh360.dto.LoginResponse;
import com.rh360.rh360.service.AuthService;

/**
 * Testes unitários para a classe AuthController.
 * 
 * Esta classe testa os endpoints de autenticação, incluindo:
 * - Login bem-sucedido
 * - Falhas de autenticação
 * - Logout
 * 
 * @author Sistema RH360
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - AuthController")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private UUID userId;

    /**
     * Configuração inicial antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        loginRequest = new LoginRequest();
        loginRequest.setEmail("usuario@teste.com");
        loginRequest.setPassword("senha123");

        loginResponse = new LoginResponse(userId, "token-jwt-gerado");
    }

    @Test
    @DisplayName("Deve realizar login com sucesso e retornar 200")
    void deveRealizarLoginComSucesso() {
        // Arrange
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof LoginResponse);
        
        LoginResponse body = (LoginResponse) response.getBody();
        assertEquals(userId, body.getId());
        assertEquals("token-jwt-gerado", body.getToken());
        
        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    @DisplayName("Deve retornar 401 quando usuário não for encontrado")
    void deveRetornar401QuandoUsuarioNaoEncontrado() {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Usuário não encontrado"));

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Usuário não encontrado"));
        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    @DisplayName("Deve retornar 401 quando senha estiver incorreta")
    void deveRetornar401QuandoSenhaIncorreta() {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Email ou Senha incorreta"));

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Email ou Senha incorreta"));
        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    @DisplayName("Deve retornar 401 quando usuário estiver inativo")
    void deveRetornar401QuandoUsuarioInativo() {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Usuário inativo"));

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Usuário inativo"));
        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    @DisplayName("Deve realizar logout com sucesso quando token é válido")
    void deveRealizarLogoutComSucesso() {
        // Arrange
        String authHeader = "Bearer token-jwt-valido";
        doNothing().when(authService).logout(anyString());

        // Act
        ResponseEntity<?> response = authController.logout(authHeader);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Logout realizado com sucesso"));
        verify(authService, times(1)).logout("token-jwt-valido");
    }

    @Test
    @DisplayName("Deve retornar 401 quando header Authorization não for fornecido")
    void deveRetornar401QuandoHeaderNaoFornecido() {
        // Act
        ResponseEntity<?> response = authController.logout(null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Token não fornecido ou formato inválido"));
        verify(authService, never()).logout(anyString());
    }

    @Test
    @DisplayName("Deve retornar 401 quando header Authorization não começar com 'Bearer '")
    void deveRetornar401QuandoHeaderFormatoInvalido() {
        // Arrange
        String authHeader = "Invalid token-jwt-valido";

        // Act
        ResponseEntity<?> response = authController.logout(authHeader);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Token não fornecido ou formato inválido"));
        verify(authService, never()).logout(anyString());
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorrer erro durante logout")
    void deveRetornar500QuandoErroNoLogout() {
        // Arrange
        String authHeader = "Bearer token-jwt-valido";
        doThrow(new RuntimeException("Erro ao processar logout"))
            .when(authService).logout(anyString());

        // Act
        ResponseEntity<?> response = authController.logout(authHeader);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Erro ao realizar logout"));
        verify(authService, times(1)).logout("token-jwt-valido");
    }
}
