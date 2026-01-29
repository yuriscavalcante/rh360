package com.rh360.rh360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.rh360.rh360.dto.UserResponse;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.repository.PermissionRepository;
import com.rh360.rh360.repository.UsersRepository;
import com.rh360.rh360.realtime.NoOpRealTimePublisher;
import com.rh360.rh360.realtime.RealTimePublisher;

/**
 * Testes unitários para a classe UsersService.
 * 
 * Esta classe testa todas as operações CRUD de usuários, incluindo:
 * - Criação de usuário
 * - Listagem paginada
 * - Busca por ID
 * - Atualização
 * - Soft delete
 * - Validações de email duplicado
 * 
 * @author Sistema RH360
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - UsersService")
class UsersServiceTest {

    @Mock
    private UsersRepository repository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private R2StorageService r2StorageService;

    @Mock
    private CompreFaceService compreFaceService;

    @Mock
    private RealTimePublisher realTimePublisher;

    @InjectMocks
    private UsersService usersService;

    private User user;
    private UUID userId;

    /**
     * Configuração inicial antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setName("João Silva");
        user.setEmail("joao@teste.com");
        user.setPassword("senha123");
        user.setRole("user");
        user.setStatus("active");
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void deveCriarUsuarioComSucesso() {
        // Arrange
        when(repository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertNotNull(savedUser.getCreatedAt());
            assertNotNull(savedUser.getUpdatedAt());
            assertEquals("active", savedUser.getStatus());
            assertEquals("user", savedUser.getRole());
            assertNotEquals("senha123", savedUser.getPassword()); // Senha deve estar criptografada
            return savedUser;
        });

        // Act
        User result = usersService.create(user);

        // Assert
        assertNotNull(result);
        verify(repository, times(1)).findByEmail(user.getEmail());
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar usuário com email duplicado")
    void deveLancarExcecaoQuandoEmailDuplicado() {
        // Arrange
        when(repository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usersService.create(user);
        });

        assertEquals("Email já cadastrado", exception.getMessage());
        verify(repository, times(1)).findByEmail(user.getEmail());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve retornar lista paginada de usuários")
    void deveRetornarListaPaginadaDeUsuarios() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = new ArrayList<>();
        users.add(user);
        
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setName("Maria Santos");
        user2.setEmail("maria@teste.com");
        users.add(user2);

        Page<User> userPage = new PageImpl<>(users, pageable, 2);
        when(repository.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<UserResponse> result = usersService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar usuário quando encontrado por ID")
    void deveRetornarUsuarioQuandoEncontradoPorId() {
        // Arrange
        when(repository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        User result = usersService.findById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("João Silva", result.getName());
        verify(repository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Deve retornar null quando usuário não for encontrado por ID")
    void deveRetornarNullQuandoUsuarioNaoEncontrado() {
        // Arrange
        UUID inexistenteId = UUID.randomUUID();
        when(repository.findById(inexistenteId)).thenReturn(Optional.empty());

        // Act
        User result = usersService.findById(inexistenteId);

        // Assert
        assertNull(result);
        verify(repository, times(1)).findById(inexistenteId);
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void deveAtualizarUsuarioComSucesso() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setName("João Silva Atualizado");
        updatedUser.setEmail("joao.novo@teste.com");
        updatedUser.setRole("admin");
        updatedUser.setStatus("active");

        User savedUser = new User();
        savedUser.setId(userId);
        savedUser.setName("João Silva Atualizado");
        savedUser.setEmail("joao.novo@teste.com");
        savedUser.setRole("admin");
        savedUser.setStatus("active");

        when(repository.findById(userId))
            .thenReturn(Optional.of(user))  // Primeira chamada: busca usuário existente
            .thenReturn(Optional.of(savedUser)); // Segunda chamada: recarrega após salvar
        when(repository.findByEmail(updatedUser.getEmail())).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            assertNotNull(saved.getUpdatedAt());
            return saved;
        });

        // Act
        User result = usersService.update(userId, updatedUser);

        // Assert
        assertNotNull(result);
        assertEquals("João Silva Atualizado", result.getName());
        assertEquals("joao.novo@teste.com", result.getEmail());
        assertEquals("admin", result.getRole());
        verify(repository, times(2)).findById(userId); // Duas chamadas: início e fim
        verify(repository, times(1)).findByEmail(updatedUser.getEmail());
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar usuário inexistente")
    void deveLancarExcecaoAoAtualizarUsuarioInexistente() {
        // Arrange
        UUID inexistenteId = UUID.randomUUID();
        when(repository.findById(inexistenteId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usersService.update(inexistenteId, user);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(repository, times(1)).findById(inexistenteId);
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar email para um já existente")
    void deveLancarExcecaoAoAtualizarEmailParaExistente() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setEmail("email.existente@teste.com");

        User existingUserWithEmail = new User();
        existingUserWithEmail.setId(UUID.randomUUID());
        existingUserWithEmail.setEmail("email.existente@teste.com");

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findByEmail("email.existente@teste.com")).thenReturn(Optional.of(existingUserWithEmail));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usersService.update(userId, updatedUser);
        });

        assertEquals("Email já cadastrado", exception.getMessage());
        verify(repository, times(1)).findById(userId);
        verify(repository, times(1)).findByEmail("email.existente@teste.com");
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve permitir atualizar usuário mantendo o mesmo email")
    void devePermitirAtualizarUsuarioMantendoMesmoEmail() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setName("João Silva Atualizado");
        updatedUser.setEmail(user.getEmail()); // Mesmo email
        updatedUser.setRole("admin");

        User savedUser = new User();
        savedUser.setId(userId);
        savedUser.setName("João Silva Atualizado");
        savedUser.setEmail(user.getEmail());
        savedUser.setRole("admin");

        when(repository.findById(userId))
            .thenReturn(Optional.of(user))  // Primeira chamada: busca usuário existente
            .thenReturn(Optional.of(savedUser)); // Segunda chamada: recarrega após salvar
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = usersService.update(userId, updatedUser);

        // Assert
        assertNotNull(result);
        assertEquals("João Silva Atualizado", result.getName());
        verify(repository, times(2)).findById(userId); // Duas chamadas: início e fim
        verify(repository, never()).findByEmail(anyString()); // Não deve verificar email duplicado
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve realizar soft delete de usuário")
    void deveRealizarSoftDeleteDeUsuario() {
        // Arrange
        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals("deleted", savedUser.getStatus());
            assertNotNull(savedUser.getUpdatedAt());
            return savedUser;
        });

        // Act
        usersService.delete(userId);

        // Assert
        verify(repository, times(1)).findById(userId);
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar usuário inexistente")
    void deveLancarExcecaoAoDeletarUsuarioInexistente() {
        // Arrange
        UUID inexistenteId = UUID.randomUUID();
        when(repository.findById(inexistenteId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usersService.delete(inexistenteId);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(repository, times(1)).findById(inexistenteId);
        verify(repository, never()).save(any(User.class));
    }
}
