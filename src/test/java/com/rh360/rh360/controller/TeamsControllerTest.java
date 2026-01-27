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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.rh360.rh360.dto.TeamRequest;
import com.rh360.rh360.dto.TeamResponse;
import com.rh360.rh360.entity.Team;
import com.rh360.rh360.service.TeamService;

/**
 * Testes unitários para a classe TeamsController.
 * 
 * Esta classe testa todos os endpoints de gerenciamento de equipes, incluindo:
 * - Criação de equipe
 * - Listagem paginada
 * - Busca por ID
 * - Atualização
 * - Exclusão
 * - Adição e remoção de usuários
 * 
 * @author Sistema RH360
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - TeamsController")
class TeamsControllerTest {

    @Mock
    private TeamService teamService;

    @InjectMocks
    private TeamsController teamsController;

    private TeamRequest teamRequest;
    private TeamResponse teamResponse;
    private UUID teamId;
    private UUID userId;
    private Pageable pageable;

    /**
     * Configuração inicial antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        teamId = UUID.randomUUID();
        userId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);

        teamRequest = new TeamRequest();
        teamRequest.setName("Equipe de Desenvolvimento");
        teamRequest.setDescription("Equipe responsável pelo desenvolvimento");

        Team team = new Team();
        team.setId(teamId);
        team.setName("Equipe de Desenvolvimento");
        team.setDescription("Equipe responsável pelo desenvolvimento");
        team.setStatus("active");
        teamResponse = new TeamResponse(team);
    }

    @Test
    @DisplayName("Deve criar equipe com sucesso")
    void deveCriarEquipeComSucesso() {
        // Arrange
        when(teamService.create(any(TeamRequest.class))).thenReturn(teamResponse);

        // Act
        TeamResponse result = teamsController.create(teamRequest);

        // Assert
        assertNotNull(result);
        assertEquals(teamId, result.getId());
        assertEquals("Equipe de Desenvolvimento", result.getName());
        verify(teamService, times(1)).create(teamRequest);
    }

    @Test
    @DisplayName("Deve retornar lista paginada de equipes")
    @SuppressWarnings("unchecked")
    void deveRetornarListaPaginadaDeEquipes() {
        // Arrange
        Page<TeamResponse> teamPage = mock(Page.class);
        when(teamService.findAll(any(Pageable.class), nullable(String.class))).thenReturn(teamPage);

        // Act
        Page<TeamResponse> result = teamsController.findAll(pageable, null);

        // Assert
        assertNotNull(result);
        verify(teamService, times(1)).findAll(pageable, null);
    }

    @Test
    @DisplayName("Deve retornar equipe quando encontrada por ID")
    void deveRetornarEquipeQuandoEncontradaPorId() {
        // Arrange
        when(teamService.findById(teamId)).thenReturn(teamResponse);

        // Act
        TeamResponse result = teamsController.findById(teamId);

        // Assert
        assertNotNull(result);
        assertEquals(teamId, result.getId());
        verify(teamService, times(1)).findById(teamId);
    }

    @Test
    @DisplayName("Deve atualizar equipe com sucesso")
    void deveAtualizarEquipeComSucesso() {
        // Arrange
        TeamRequest updatedRequest = new TeamRequest();
        updatedRequest.setName("Equipe Atualizada");
        
        Team updatedTeam = new Team();
        updatedTeam.setId(teamId);
        updatedTeam.setName("Equipe Atualizada");
        updatedTeam.setStatus("active");
        TeamResponse updatedResponse = new TeamResponse(updatedTeam);
        
        when(teamService.update(eq(teamId), any(TeamRequest.class))).thenReturn(updatedResponse);

        // Act
        TeamResponse result = teamsController.update(teamId, updatedRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Equipe Atualizada", result.getName());
        verify(teamService, times(1)).update(teamId, updatedRequest);
    }

    @Test
    @DisplayName("Deve deletar equipe com sucesso")
    void deveDeletarEquipeComSucesso() {
        // Arrange
        doNothing().when(teamService).delete(teamId);

        // Act
        teamsController.delete(teamId);

        // Assert
        verify(teamService, times(1)).delete(teamId);
    }

    @Test
    @DisplayName("Deve adicionar usuário à equipe com sucesso")
    void deveAdicionarUsuarioAEquipe() {
        // Arrange
        doNothing().when(teamService).addUserToTeam(teamId, userId);

        // Act
        teamsController.addUserToTeam(teamId, userId);

        // Assert
        verify(teamService, times(1)).addUserToTeam(teamId, userId);
    }

    @Test
    @DisplayName("Deve remover usuário da equipe com sucesso")
    void deveRemoverUsuarioDaEquipe() {
        // Arrange
        doNothing().when(teamService).removeUserFromTeam(teamId, userId);

        // Act
        teamsController.removeUserFromTeam(teamId, userId);

        // Assert
        verify(teamService, times(1)).removeUserFromTeam(teamId, userId);
    }
}
