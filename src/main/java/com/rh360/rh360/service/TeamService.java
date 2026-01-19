package com.rh360.rh360.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rh360.rh360.dto.TeamRequest;
import com.rh360.rh360.dto.TeamResponse;
import com.rh360.rh360.entity.Team;
import com.rh360.rh360.entity.TeamUser;
import com.rh360.rh360.repository.TeamRepository;
import com.rh360.rh360.repository.UsersRepository;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UsersRepository usersRepository;

    public TeamService(TeamRepository teamRepository, UsersRepository usersRepository) {
        this.teamRepository = teamRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public TeamResponse create(TeamRequest request) {
        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setStatus("active");
        team.setCreatedAt(LocalDateTime.now().toString());
        team.setUpdatedAt(LocalDateTime.now().toString());
        team.setTeamUsers(new ArrayList<>());
        
        Team savedTeam = teamRepository.save(team);
        
        // Obter lista de IDs de usuários (suporta tanto "users" quanto "userIds")
        List<UUID> userIds = request.getUsers() != null ? request.getUsers() : request.getUserIds();
        
        // Associar usuários se fornecidos
        if (userIds != null && !userIds.isEmpty()) {
            for (UUID userId : userIds) {
                com.rh360.rh360.entity.User user = usersRepository.getReferenceById(userId);
                
                TeamUser teamUser = new TeamUser();
                teamUser.setId(new com.rh360.rh360.entity.TeamUserId(savedTeam.getId(), userId));
                teamUser.setTeam(savedTeam);
                teamUser.setUser(user);
                savedTeam.getTeamUsers().add(teamUser);
            }
            // Forçar flush para garantir que os relacionamentos sejam persistidos
            teamRepository.saveAndFlush(savedTeam);
        }
        
        return new TeamResponse(savedTeam, false);
    }

    public Page<TeamResponse> findAll(Pageable pageable) {
        Page<Team> teamPage = teamRepository.findAll(pageable);
        List<TeamResponse> teamResponses = teamPage.getContent().stream()
            .map(team -> new TeamResponse(team, false))
            .collect(Collectors.toList());
        return new PageImpl<>(teamResponses, pageable, teamPage.getTotalElements());
    }

    public TeamResponse findById(UUID id) {
        Team team = teamRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));
        return new TeamResponse(team, true);
    }

    @Transactional
    public TeamResponse update(UUID id, TeamRequest request) {
        Team existingTeam = teamRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));
        
        existingTeam.setName(request.getName());
        existingTeam.setDescription(request.getDescription());
        existingTeam.setUpdatedAt(LocalDateTime.now().toString());
        
        // Obter lista de IDs de usuários (suporta tanto "users" quanto "userIds")
        List<UUID> userIds = request.getUsers() != null ? request.getUsers() : request.getUserIds();
        
        // Atualizar relacionamentos com usuários
        if (userIds != null) {
            // Limpar relacionamentos existentes
            if (existingTeam.getTeamUsers() == null) {
                existingTeam.setTeamUsers(new ArrayList<>());
            } else {
                existingTeam.getTeamUsers().clear();
            }
            
            // Criar novos relacionamentos
            if (!userIds.isEmpty()) {
                for (UUID userId : userIds) {
                    com.rh360.rh360.entity.User user = usersRepository.getReferenceById(userId);
                    
                    TeamUser teamUser = new TeamUser();
                    teamUser.setId(new com.rh360.rh360.entity.TeamUserId(id, userId));
                    teamUser.setTeam(existingTeam);
                    teamUser.setUser(user);
                    existingTeam.getTeamUsers().add(teamUser);
                }
            }
        }
        
        Team savedTeam = teamRepository.saveAndFlush(existingTeam);
        return new TeamResponse(savedTeam, false);
    }

    @Transactional
    public void delete(UUID id) {
        Team existingTeam = teamRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));
        
        // Alterar status para inactive
        existingTeam.setStatus("inactive");
        existingTeam.setUpdatedAt(LocalDateTime.now().toString());
        teamRepository.save(existingTeam);
    }

    @Transactional
    public void addUserToTeam(UUID teamId, UUID userId) {
        // Verificar se a equipe existe
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));
        
        // Verificar se o usuário existe
        com.rh360.rh360.entity.User user = usersRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Verificar se já existe o relacionamento
        if (team.getTeamUsers() == null) {
            team.setTeamUsers(new ArrayList<>());
        }
        
        boolean alreadyExists = team.getTeamUsers().stream()
            .anyMatch(tu -> tu.getId().getUserId().equals(userId));
        
        if (alreadyExists) {
            throw new RuntimeException("Usuário já está associado a esta equipe");
        }
        
        TeamUser teamUser = new TeamUser();
        teamUser.setId(new com.rh360.rh360.entity.TeamUserId(teamId, userId));
        teamUser.setTeam(team);
        teamUser.setUser(user);
        team.getTeamUsers().add(teamUser);
        teamRepository.save(team);
    }

    @Transactional
    public void removeUserFromTeam(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));
        
        if (team.getTeamUsers() != null) {
            team.getTeamUsers().removeIf(tu -> tu.getId().getUserId().equals(userId));
            teamRepository.save(team);
        }
    }
}
