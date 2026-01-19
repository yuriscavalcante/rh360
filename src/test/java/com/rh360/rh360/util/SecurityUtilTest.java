package com.rh360.rh360.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Testes unitários para a classe SecurityUtil.
 * 
 * Esta classe testa os métodos utilitários de segurança, incluindo:
 * - Extração de userId do request
 * - Extração de email do request
 * - Extração de role do request
 * - Comportamento quando atributos não estão presentes
 * 
 * @author Sistema RH360
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - SecurityUtil")
class SecurityUtilTest {

    @Mock
    private HttpServletRequest request;

    private UUID userId;
    private String email;
    private String role;

    /**
     * Configuração inicial antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "usuario@teste.com";
        role = "admin";
    }

    @Test
    @DisplayName("Deve extrair userId quando presente no request")
    void deveExtrairUserIdQuandoPresente() {
        // Arrange
        when(request.getAttribute("userId")).thenReturn(userId);

        // Act
        UUID result = SecurityUtil.getUserId(request);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result);
        verify(request, times(1)).getAttribute("userId");
    }

    @Test
    @DisplayName("Deve retornar null quando userId não estiver presente no request")
    void deveRetornarNullQuandoUserIdNaoPresente() {
        // Arrange
        when(request.getAttribute("userId")).thenReturn(null);

        // Act
        UUID result = SecurityUtil.getUserId(request);

        // Assert
        assertNull(result);
        verify(request, times(1)).getAttribute("userId");
    }

    @Test
    @DisplayName("Deve extrair email quando presente no request")
    void deveExtrairEmailQuandoPresente() {
        // Arrange
        when(request.getAttribute("email")).thenReturn(email);

        // Act
        String result = SecurityUtil.getEmail(request);

        // Assert
        assertNotNull(result);
        assertEquals(email, result);
        verify(request, times(1)).getAttribute("email");
    }

    @Test
    @DisplayName("Deve retornar null quando email não estiver presente no request")
    void deveRetornarNullQuandoEmailNaoPresente() {
        // Arrange
        when(request.getAttribute("email")).thenReturn(null);

        // Act
        String result = SecurityUtil.getEmail(request);

        // Assert
        assertNull(result);
        verify(request, times(1)).getAttribute("email");
    }

    @Test
    @DisplayName("Deve extrair role quando presente no request")
    void deveExtrairRoleQuandoPresente() {
        // Arrange
        when(request.getAttribute("role")).thenReturn(role);

        // Act
        String result = SecurityUtil.getRole(request);

        // Assert
        assertNotNull(result);
        assertEquals(role, result);
        verify(request, times(1)).getAttribute("role");
    }

    @Test
    @DisplayName("Deve retornar null quando role não estiver presente no request")
    void deveRetornarNullQuandoRoleNaoPresente() {
        // Arrange
        when(request.getAttribute("role")).thenReturn(null);

        // Act
        String result = SecurityUtil.getRole(request);

        // Assert
        assertNull(result);
        verify(request, times(1)).getAttribute("role");
    }

    @Test
    @DisplayName("Deve retornar null quando atributo não for do tipo esperado")
    void deveRetornarNullQuandoAtributoTipoIncorreto() {
        // Arrange
        when(request.getAttribute("userId")).thenReturn("string-invalida");

        // Act
        UUID result = SecurityUtil.getUserId(request);

        // Assert
        assertNull(result);
        verify(request, times(1)).getAttribute("userId");
    }
}
