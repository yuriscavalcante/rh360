package com.rh360.rh360.filter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rh360.rh360.service.TokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Testes unitários para a classe JwtAuthenticationFilter.
 * 
 * Esta classe testa o filtro de autenticação JWT, incluindo:
 * - Permissão de acesso a paths excluídos
 * - Validação de token no header Authorization
 * - Rejeição de requisições sem token
 * - Rejeição de requisições com token inválido
 * - Adição de atributos do usuário ao request quando autenticado
 * 
 * @author Sistema RH360
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID userId;
    private String email;
    private String role;
    private String token;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    /**
     * Configuração inicial antes de cada teste.
     */
    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();
        email = "usuario@teste.com";
        role = "user";
        token = "token-jwt-valido";
        
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    @DisplayName("Deve permitir acesso a path excluído sem autenticação")
    void devePermitirAcessoAPathExcluido() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(tokenService, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Deve permitir acesso a swagger-ui sem autenticação")
    void devePermitirAcessoASwagger() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(tokenService, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Deve permitir acesso a api-docs sem autenticação")
    void devePermitirAcessoAApiDocs() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/v3/api-docs");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(tokenService, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Deve rejeitar requisição sem header Authorization")
    void deveRejeitarRequisicaoSemHeader() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(1)).setContentType("application/json");
        verify(filterChain, never()).doFilter(any(), any());
        verify(tokenService, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Deve rejeitar requisição com header Authorization em formato inválido")
    void deveRejeitarRequisicaoComHeaderFormatoInvalido() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn("Invalid token");
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(1)).setContentType("application/json");
        verify(filterChain, never()).doFilter(any(), any());
        verify(tokenService, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Deve rejeitar requisição com token inválido")
    void deveRejeitarRequisicaoComTokenInvalido() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenService.validateToken(token)).thenReturn(false);
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(1)).setContentType("application/json");
        verify(tokenService, times(1)).validateToken(token);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Deve permitir acesso quando token é válido e adicionar atributos ao request")
    void devePermitirAcessoQuandoTokenValido() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenService.validateToken(token)).thenReturn(true);
        when(tokenService.extractUserId(token)).thenReturn(userId);
        when(tokenService.extractEmail(token)).thenReturn(email);
        when(tokenService.extractRole(token)).thenReturn(role);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenService, times(1)).validateToken(token);
        verify(tokenService, times(1)).extractUserId(token);
        verify(tokenService, times(1)).extractEmail(token);
        verify(tokenService, times(1)).extractRole(token);
        verify(request, times(1)).setAttribute("userId", userId);
        verify(request, times(1)).setAttribute("email", email);
        verify(request, times(1)).setAttribute("role", role);
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("Deve rejeitar requisição quando ocorrer exceção durante validação")
    void deveRejeitarRequisicaoQuandoOcorrerExcecao() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenService.validateToken(token)).thenThrow(new RuntimeException("Erro ao validar token"));
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(1)).setContentType("application/json");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Deve extrair token corretamente removendo prefixo 'Bearer '")
    void deveExtrairTokenCorretamente() throws Exception {
        // Arrange
        String authHeader = "Bearer " + token;
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(tokenService.validateToken(token)).thenReturn(true);
        when(tokenService.extractUserId(token)).thenReturn(userId);
        when(tokenService.extractEmail(token)).thenReturn(email);
        when(tokenService.extractRole(token)).thenReturn(role);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenService, times(1)).validateToken(token);
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
