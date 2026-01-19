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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.rh360.rh360.dto.UserResponse;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.service.UsersService;
import com.rh360.rh360.util.SecurityUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Testes unitários para a classe UsersController.
 * 
 * Esta classe testa todos os endpoints de gerenciamento de usuários, incluindo:
 * - Criação de usuário
 * - Listagem paginada
 * - Busca por ID
 * - Busca do usuário atual
 * - Atualização
 * - Exclusão
 * 
 * @author Sistema RH360
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - UsersController")
class UsersControllerTest {

    @Mock
    private UsersService usersService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UsersController usersController;

    private User user;
    private UUID userId;
    private Pageable pageable;

    /**
     * Configuração inicial antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);

        user = new User();
        user.setId(userId);
        user.setName("João Silva");
        user.setEmail("joao@teste.com");
        user.setRole("user");
        user.setStatus("active");
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void deveCriarUsuarioComSucesso() {
        // Arrange
        when(usersService.create(any(User.class))).thenReturn(user);

        // Act
        User result = usersController.create(user);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("João Silva", result.getName());
        verify(usersService, times(1)).create(user);
    }

    @Test
    @DisplayName("Deve retornar lista paginada de usuários")
    @SuppressWarnings("unchecked")
    void deveRetornarListaPaginadaDeUsuarios() {
        // Arrange
        Page<UserResponse> userPage = mock(Page.class);
        when(usersService.findAll(any(Pageable.class))).thenReturn(userPage);

        // Act
        Page<UserResponse> result = usersController.findAll(pageable);

        // Assert
        assertNotNull(result);
        verify(usersService, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar usuário quando encontrado por ID")
    void deveRetornarUsuarioQuandoEncontradoPorId() {
        // Arrange
        when(usersService.findById(userId)).thenReturn(user);

        // Act
        User result = usersController.findById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(usersService, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Deve retornar usuário atual quando autenticado")
    void deveRetornarUsuarioAtualQuandoAutenticado() {
        // Arrange
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(() -> SecurityUtil.getUserId(request)).thenReturn(userId);
            when(usersService.findById(userId)).thenReturn(user);

            // Act
            User result = usersController.getCurrentUser(request);

            // Assert
            assertNotNull(result);
            assertEquals(userId, result.getId());
            mockedSecurityUtil.verify(() -> SecurityUtil.getUserId(request), times(1));
            verify(usersService, times(1)).findById(userId);
        }
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não estiver autenticado")
    void deveLancarExcecaoQuandoUsuarioNaoAutenticado() {
        // Arrange
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(() -> SecurityUtil.getUserId(request)).thenReturn(null);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                usersController.getCurrentUser(request);
            });

            assertEquals("Usuário não autenticado", exception.getMessage());
            mockedSecurityUtil.verify(() -> SecurityUtil.getUserId(request), times(1));
            verify(usersService, never()).findById(any());
        }
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void deveAtualizarUsuarioComSucesso() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setName("João Silva Atualizado");
        updatedUser.setEmail("joao.novo@teste.com");
        
        when(usersService.update(eq(userId), any(User.class))).thenReturn(updatedUser);

        // Act
        User result = usersController.update(userId, updatedUser);

        // Assert
        assertNotNull(result);
        verify(usersService, times(1)).update(userId, updatedUser);
    }

    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void deveDeletarUsuarioComSucesso() {
        // Arrange
        doNothing().when(usersService).delete(userId);

        // Act
        usersController.delete(userId);

        // Assert
        verify(usersService, times(1)).delete(userId);
    }
}
