package com.rh360.rh360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.rh360.rh360.dto.TeamRequest;
import com.rh360.rh360.dto.TeamResponse;
import com.rh360.rh360.entity.Team;
import com.rh360.rh360.entity.TeamUser;
import com.rh360.rh360.entity.TeamUserId;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.repository.TeamRepository;
import com.rh360.rh360.repository.UsersRepository;

/**
 * Testes unitários para a classe TeamService.
 * 
 * Esta classe testa todas as operações relacionadas a equipes, incluindo:
 * - Criação de equipe
 * - Listagem paginada
 * - Busca por ID
 * - Atualização
 * - Soft delete
 * - Adição e remoção de usuários
 * 
 * @author Sistema RH360
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - TeamService")
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private TeamService teamService;

    private Team team;
    private UUID teamId;
    private User user1;
    private User user2;
    private UUID userId1;
    private UUID userId2;

    /**
     * Configuração inicial antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        teamId = UUID.randomUUID();
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();

        team = new Team();
        team.setId(teamId);
        team.setName("Equipe de Desenvolvimento");
        team.setDescription("Equipe responsável pelo desenvolvimento");
        team.setStatus("active");
        team.setTeamUsers(new ArrayList<>());

        user1 = new User();
        user1.setId(userId1);
        user1.setName("João Silva");
        user1.setEmail("joao@teste.com");

        user2 = new User();
        user2.setId(userId2);
        user2.setName("Maria Santos");
        user2.setEmail("maria@teste.com");
    }

    @Test
    @DisplayName("Deve criar equipe com sucesso sem usuários")
    void deveCriarEquipeSemUsuarios() {
        // Arrange
        TeamRequest request = new TeamRequest();
        request.setName("Nova Equipe");
        request.setDescription("Descrição da nova equipe");
        request.setUserIds(null);

        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team savedTeam = invocation.getArgument(0);
            savedTeam.setId(teamId);
            assertNotNull(savedTeam.getCreatedAt());
            assertNotNull(savedTeam.getUpdatedAt());
            assertEquals("active", savedTeam.getStatus());
            return savedTeam;
        });

        // Act
        TeamResponse response = teamService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals("Nova Equipe", response.getName());
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    @DisplayName("Deve criar equipe com usuários associados")
    void deveCriarEquipeComUsuarios() {
        // Arrange
        TeamRequest request = new TeamRequest();
        request.setName("Nova Equipe");
        request.setDescription("Descrição da nova equipe");
        request.setUserIds(Arrays.asList(userId1, userId2));

        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team savedTeam = invocation.getArgument(0);
            savedTeam.setId(teamId);
            return savedTeam;
        });
        when(usersRepository.getReferenceById(userId1)).thenReturn(user1);
        when(usersRepository.getReferenceById(userId2)).thenReturn(user2);
        when(teamRepository.saveAndFlush(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TeamResponse response = teamService.create(request);

        // Assert
        assertNotNull(response);
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(usersRepository, times(1)).getReferenceById(userId1);
        verify(usersRepository, times(1)).getReferenceById(userId2);
        verify(teamRepository, times(1)).saveAndFlush(any(Team.class));
    }

    @Test
    @DisplayName("Deve criar equipe usando campo 'users' ao invés de 'userIds'")
    void deveCriarEquipeUsandoCampoUsers() {
        // Arrange
        TeamRequest request = new TeamRequest();
        request.setName("Nova Equipe");
        request.setUsers(Arrays.asList(userId1));

        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team savedTeam = invocation.getArgument(0);
            savedTeam.setId(teamId);
            return savedTeam;
        });
        when(usersRepository.getReferenceById(userId1)).thenReturn(user1);
        when(teamRepository.saveAndFlush(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TeamResponse response = teamService.create(request);

        // Assert
        assertNotNull(response);
        verify(usersRepository, times(1)).getReferenceById(userId1);
    }

    @Test
    @DisplayName("Deve retornar lista paginada de equipes")
    void deveRetornarListaPaginadaDeEquipes() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Team> teams = Arrays.asList(team);
        Page<Team> teamPage = new PageImpl<>(teams, pageable, 1);

        when(teamRepository.findAll(pageable)).thenReturn(teamPage);

        // Act
        Page<TeamResponse> result = teamService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(teamRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar equipe quando encontrada por ID")
    void deveRetornarEquipeQuandoEncontradaPorId() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        // Act
        TeamResponse response = teamService.findById(teamId);

        // Assert
        assertNotNull(response);
        assertEquals(teamId, response.getId());
        verify(teamRepository, times(1)).findById(teamId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando equipe não for encontrada por ID")
    void deveLancarExcecaoQuandoEquipeNaoEncontrada() {
        // Arrange
        UUID inexistenteId = UUID.randomUUID();
        when(teamRepository.findById(inexistenteId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teamService.findById(inexistenteId);
        });

        assertEquals("Equipe não encontrada", exception.getMessage());
        verify(teamRepository, times(1)).findById(inexistenteId);
    }

    @Test
    @DisplayName("Deve atualizar equipe com sucesso")
    void deveAtualizarEquipeComSucesso() {
        // Arrange
        TeamRequest request = new TeamRequest();
        request.setName("Equipe Atualizada");
        request.setDescription("Nova descrição");
        request.setUserIds(Arrays.asList(userId1));

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(usersRepository.getReferenceById(userId1)).thenReturn(user1);
        when(teamRepository.saveAndFlush(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TeamResponse response = teamService.update(teamId, request);

        // Assert
        assertNotNull(response);
        verify(teamRepository, times(1)).findById(teamId);
        verify(teamRepository, times(1)).saveAndFlush(any(Team.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar equipe inexistente")
    void deveLancarExcecaoAoAtualizarEquipeInexistente() {
        // Arrange
        UUID inexistenteId = UUID.randomUUID();
        TeamRequest request = new TeamRequest();
        request.setName("Equipe Atualizada");

        when(teamRepository.findById(inexistenteId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teamService.update(inexistenteId, request);
        });

        assertEquals("Equipe não encontrada", exception.getMessage());
        verify(teamRepository, times(1)).findById(inexistenteId);
        verify(teamRepository, never()).saveAndFlush(any(Team.class));
    }

    @Test
    @DisplayName("Deve atualizar equipe removendo todos os usuários quando lista vazia for fornecida")
    void deveAtualizarEquipeRemovendoUsuarios() {
        // Arrange
        TeamUser teamUser = new TeamUser();
        teamUser.setId(new TeamUserId(teamId, userId1));
        team.getTeamUsers().add(teamUser);

        TeamRequest request = new TeamRequest();
        request.setName("Equipe Atualizada");
        request.setUserIds(new ArrayList<>()); // Lista vazia

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamRepository.saveAndFlush(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TeamResponse response = teamService.update(teamId, request);

        // Assert
        assertNotNull(response);
        verify(teamRepository, times(1)).saveAndFlush(any(Team.class));
    }

    @Test
    @DisplayName("Deve realizar soft delete de equipe")
    void deveRealizarSoftDeleteDeEquipe() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team savedTeam = invocation.getArgument(0);
            assertEquals("inactive", savedTeam.getStatus());
            assertNotNull(savedTeam.getUpdatedAt());
            return savedTeam;
        });

        // Act
        teamService.delete(teamId);

        // Assert
        verify(teamRepository, times(1)).findById(teamId);
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar equipe inexistente")
    void deveLancarExcecaoAoDeletarEquipeInexistente() {
        // Arrange
        UUID inexistenteId = UUID.randomUUID();
        when(teamRepository.findById(inexistenteId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teamService.delete(inexistenteId);
        });

        assertEquals("Equipe não encontrada", exception.getMessage());
        verify(teamRepository, times(1)).findById(inexistenteId);
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @DisplayName("Deve adicionar usuário à equipe com sucesso")
    void deveAdicionarUsuarioAEquipe() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(usersRepository.findById(userId1)).thenReturn(Optional.of(user1));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        teamService.addUserToTeam(teamId, userId1);

        // Assert
        verify(teamRepository, times(1)).findById(teamId);
        verify(usersRepository, times(1)).findById(userId1);
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar adicionar usuário já associado à equipe")
    void deveLancarExcecaoAoAdicionarUsuarioJaAssociado() {
        // Arrange
        TeamUser existingTeamUser = new TeamUser();
        existingTeamUser.setId(new TeamUserId(teamId, userId1));
        existingTeamUser.setTeam(team);
        existingTeamUser.setUser(user1);
        team.getTeamUsers().add(existingTeamUser);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(usersRepository.findById(userId1)).thenReturn(Optional.of(user1));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teamService.addUserToTeam(teamId, userId1);
        });

        assertEquals("Usuário já está associado a esta equipe", exception.getMessage());
        verify(teamRepository, times(1)).findById(teamId);
        verify(usersRepository, times(1)).findById(userId1);
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar adicionar usuário a equipe inexistente")
    void deveLancarExcecaoAoAdicionarUsuarioAEquipeInexistente() {
        // Arrange
        UUID inexistenteId = UUID.randomUUID();
        when(teamRepository.findById(inexistenteId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teamService.addUserToTeam(inexistenteId, userId1);
        });

        assertEquals("Equipe não encontrada", exception.getMessage());
        verify(teamRepository, times(1)).findById(inexistenteId);
        verify(usersRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve remover usuário da equipe com sucesso")
    void deveRemoverUsuarioDaEquipe() {
        // Arrange
        TeamUser teamUser = new TeamUser();
        teamUser.setId(new TeamUserId(teamId, userId1));
        team.getTeamUsers().add(teamUser);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        teamService.removeUserFromTeam(teamId, userId1);

        // Assert
        verify(teamRepository, times(1)).findById(teamId);
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    @DisplayName("Deve remover usuário mesmo quando lista de teamUsers for null")
    void deveRemoverUsuarioQuandoListaNull() {
        // Arrange
        team.setTeamUsers(null);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        // Act
        teamService.removeUserFromTeam(teamId, userId1);

        // Assert
        verify(teamRepository, times(1)).findById(teamId);
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar remover usuário de equipe inexistente")
    void deveLancarExcecaoAoRemoverUsuarioDeEquipeInexistente() {
        // Arrange
        UUID inexistenteId = UUID.randomUUID();
        when(teamRepository.findById(inexistenteId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teamService.removeUserFromTeam(inexistenteId, userId1);
        });

        assertEquals("Equipe não encontrada", exception.getMessage());
        verify(teamRepository, times(1)).findById(inexistenteId);
        verify(teamRepository, never()).save(any(Team.class));
    }
}
