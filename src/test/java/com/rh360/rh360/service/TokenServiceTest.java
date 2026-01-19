package com.rh360.rh360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.rh360.rh360.entity.Token;
import com.rh360.rh360.repository.TokenRepository;

/**
 * Testes unitários para a classe TokenService.
 * 
 * Esta classe testa todas as operações relacionadas a tokens JWT, incluindo:
 * - Geração de tokens
 * - Salvamento de tokens
 * - Extração de informações do token
 * - Validação de tokens
 * - Desativação de tokens
 * 
 * @author Sistema RH360
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - TokenService")
class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private UUID userId;
    private String email;
    private String role;
    private String secret;
    private Long expiration;
    private Token tokenEntity;

    /**
     * Configuração inicial antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "usuario@teste.com";
        role = "user";
        secret = "minha-chave-secreta-muito-longa-para-ser-valida-para-hmac-sha256";
        expiration = 3600000L; // 1 hora em milissegundos

        // Configurar valores usando ReflectionTestUtils
        ReflectionTestUtils.setField(tokenService, "secret", secret);
        ReflectionTestUtils.setField(tokenService, "expiration", expiration);

        tokenEntity = new Token();
        tokenEntity.setId(1L);
        tokenEntity.setToken("token-jwt-valido");
        tokenEntity.setUserId(userId);
        tokenEntity.setActive(true);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("Deve gerar token JWT com sucesso")
    void deveGerarTokenJWTComSucesso() {
        // Act
        String token = tokenService.generateToken(userId, email, role);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        // Token JWT tem 3 partes separadas por ponto
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Deve salvar token no banco de dados")
    void deveSalvarTokenNoBanco() {
        // Arrange
        String tokenString = tokenService.generateToken(userId, email, role);
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> {
            Token savedToken = invocation.getArgument(0);
            assertEquals(tokenString, savedToken.getToken());
            assertEquals(userId, savedToken.getUserId());
            assertTrue(savedToken.getActive());
            assertNotNull(savedToken.getExpiresAt());
            return savedToken;
        });

        // Act
        Token result = tokenService.saveToken(tokenString, userId);

        // Assert
        assertNotNull(result);
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    @DisplayName("Deve extrair email do token")
    void deveExtrairEmailDoToken() {
        // Arrange
        String token = tokenService.generateToken(userId, email, role);

        // Act
        String extractedEmail = tokenService.extractEmail(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Deve extrair userId do token")
    void deveExtrairUserIdDoToken() {
        // Arrange
        String token = tokenService.generateToken(userId, email, role);

        // Act
        UUID extractedUserId = tokenService.extractUserId(token);

        // Assert
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("Deve extrair role do token")
    void deveExtrairRoleDoToken() {
        // Arrange
        String token = tokenService.generateToken(userId, email, role);

        // Act
        String extractedRole = tokenService.extractRole(token);

        // Assert
        assertEquals(role, extractedRole);
    }

    @Test
    @DisplayName("Deve extrair data de expiração do token")
    void deveExtrairDataExpiracaoDoToken() {
        // Arrange
        String token = tokenService.generateToken(userId, email, role);

        // Act
        Date expirationDate = tokenService.extractExpiration(token);

        // Assert
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    @DisplayName("Deve validar token quando token está válido e ativo")
    void deveValidarTokenQuandoValidoEAtivo() {
        // Arrange
        String token = tokenService.generateToken(userId, email, role);
        when(tokenRepository.findByTokenAndActiveTrue(token)).thenReturn(Optional.of(tokenEntity));

        // Act
        Boolean isValid = tokenService.validateToken(token);

        // Assert
        assertTrue(isValid);
        verify(tokenRepository, times(1)).findByTokenAndActiveTrue(token);
    }

    @Test
    @DisplayName("Deve retornar false quando token não está ativo no banco")
    void deveRetornarFalseQuandoTokenNaoAtivo() {
        // Arrange
        String token = tokenService.generateToken(userId, email, role);
        when(tokenRepository.findByTokenAndActiveTrue(token)).thenReturn(Optional.empty());

        // Act
        Boolean isValid = tokenService.validateToken(token);

        // Assert
        assertFalse(isValid);
        verify(tokenRepository, times(1)).findByTokenAndActiveTrue(token);
    }

    @Test
    @DisplayName("Deve validar token com email específico")
    void deveValidarTokenComEmailEspecifico() {
        // Arrange
        String token = tokenService.generateToken(userId, email, role);

        // Act
        Boolean isValid = tokenService.validateToken(token, email);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Deve retornar false quando email não corresponde")
    void deveRetornarFalseQuandoEmailNaoCorresponde() {
        // Arrange
        String token = tokenService.generateToken(userId, email, role);

        // Act
        Boolean isValid = tokenService.validateToken(token, "outro@email.com");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve desativar token específico")
    void deveDesativarTokenEspecifico() {
        // Arrange
        String token = "token-para-desativar";
        Token tokenToDeactivate = new Token();
        tokenToDeactivate.setActive(true);
        
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(tokenToDeactivate));
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> {
            Token savedToken = invocation.getArgument(0);
            assertFalse(savedToken.getActive());
            return savedToken;
        });

        // Act
        tokenService.deactivateToken(token);

        // Assert
        verify(tokenRepository, times(1)).findByToken(token);
        verify(tokenRepository, times(1)).save(tokenToDeactivate);
    }

    @Test
    @DisplayName("Deve desativar todos os tokens de um usuário")
    void deveDesativarTodosTokensDoUsuario() {
        // Arrange
        List<Token> activeTokens = new ArrayList<>();
        Token token1 = new Token();
        token1.setActive(true);
        Token token2 = new Token();
        token2.setActive(true);
        activeTokens.add(token1);
        activeTokens.add(token2);

        when(tokenRepository.findByUserIdAndActiveTrue(userId)).thenReturn(activeTokens);
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tokenService.deactivateAllUserTokens(userId);

        // Assert
        verify(tokenRepository, times(1)).findByUserIdAndActiveTrue(userId);
        verify(tokenRepository, times(2)).save(any(Token.class));
        assertFalse(token1.getActive());
        assertFalse(token2.getActive());
    }

    @Test
    @DisplayName("Deve encontrar token ativo quando existir")
    void deveEncontrarTokenAtivo() {
        // Arrange
        String token = "token-ativo";
        when(tokenRepository.findByTokenAndActiveTrue(token)).thenReturn(Optional.of(tokenEntity));

        // Act
        Optional<Token> result = tokenService.findActiveToken(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(tokenEntity, result.get());
        verify(tokenRepository, times(1)).findByTokenAndActiveTrue(token);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando token não for encontrado")
    void deveRetornarOptionalVazioQuandoTokenNaoEncontrado() {
        // Arrange
        String token = "token-inexistente";
        when(tokenRepository.findByTokenAndActiveTrue(token)).thenReturn(Optional.empty());

        // Act
        Optional<Token> result = tokenService.findActiveToken(token);

        // Assert
        assertFalse(result.isPresent());
        verify(tokenRepository, times(1)).findByTokenAndActiveTrue(token);
    }

    @Test
    @DisplayName("Deve retornar false quando ocorrer exceção na validação")
    void deveRetornarFalseQuandoOcorrerExcecao() {
        // Arrange
        // Token inválido que causará exceção ao tentar extrair expiração
        String invalidToken = "token-invalido-que-causa-excecao";
        // Não mockamos o repository pois a exceção ocorrerá antes, ao tentar parsear o token

        // Act
        Boolean isValid = tokenService.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }
}
