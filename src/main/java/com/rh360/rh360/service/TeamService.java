package com.rh360.rh360.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rh360.rh360.dto.TeamRequest;
import com.rh360.rh360.dto.TeamResponse;
import com.rh360.rh360.dto.UserResponse;
import com.rh360.rh360.entity.Team;
import com.rh360.rh360.entity.TeamUser;
import com.rh360.rh360.realtime.RealTimeEvent;
import com.rh360.rh360.realtime.RealTimeTopic;
import com.rh360.rh360.realtime.NoOpRealTimePublisher;
import com.rh360.rh360.realtime.RealTimePublisher;
import com.rh360.rh360.repository.TeamRepository;
import com.rh360.rh360.repository.TeamUserRepository;
import com.rh360.rh360.repository.UsersRepository;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UsersRepository usersRepository;
    private final TeamUserRepository teamUserRepository;
    private final RealTimePublisher realTimePublisher;

    @Autowired
    public TeamService(TeamRepository teamRepository, UsersRepository usersRepository, TeamUserRepository teamUserRepository,
                       RealTimePublisher realTimePublisher) {
        this.teamRepository = teamRepository;
        this.usersRepository = usersRepository;
        this.teamUserRepository = teamUserRepository;
        this.realTimePublisher = realTimePublisher != null ? realTimePublisher : NoOpRealTimePublisher.INSTANCE;
    }

    public TeamService(TeamRepository teamRepository, UsersRepository usersRepository, TeamUserRepository teamUserRepository) {
        this(teamRepository, usersRepository, teamUserRepository, NoOpRealTimePublisher.INSTANCE);
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
        
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.TEAMS, "refresh", null));
        return new TeamResponse(savedTeam, false);
    }

    public Page<TeamResponse> findAll(Pageable pageable) {
        return findAll(pageable, null);
    }

    public Page<TeamResponse> findAll(Pageable pageable, String search) {
        Page<Team> teamPage;
        if (search != null && !search.trim().isEmpty()) {
            teamPage = teamRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            teamPage = teamRepository.findAll(pageable);
        }
        List<TeamResponse> teamResponses = teamPage.getContent().stream()
            .map(team -> new TeamResponse(team, false))
            .collect(Collectors.toList());
        return new PageImpl<>(teamResponses, pageable, teamPage.getTotalElements());
    }

    public Page<TeamResponse> findByUserId(UUID userId, Pageable pageable) {
        // Buscar equipes associadas ao usuário
        List<TeamUser> teamUsers = teamUserRepository.findByIdUserId(userId);
        List<UUID> teamIds = teamUsers.stream()
            .map(tu -> tu.getTeam().getId())
            .collect(Collectors.toList());
        
        if (teamIds.isEmpty()) {
            // Se não houver equipes, retornar página vazia
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        // Buscar equipes pelos IDs
        List<Team> allTeams = teamRepository.findAllById(teamIds);
        
        // Aplicar ordenação se especificada
        if (pageable.getSort().isSorted()) {
            Sort sort = pageable.getSort();
            allTeams.sort((t1, t2) -> {
                for (Sort.Order order : sort) {
                    int comparison = 0;
                    String property = order.getProperty();
                    switch (property) {
                        case "name":
                            comparison = (t1.getName() != null && t2.getName() != null) 
                                ? t1.getName().compareToIgnoreCase(t2.getName()) : 0;
                            break;
                        case "createdAt":
                            comparison = (t1.getCreatedAt() != null && t2.getCreatedAt() != null) 
                                ? t1.getCreatedAt().compareTo(t2.getCreatedAt()) : 0;
                            break;
                        case "updatedAt":
                            comparison = (t1.getUpdatedAt() != null && t2.getUpdatedAt() != null) 
                                ? t1.getUpdatedAt().compareTo(t2.getUpdatedAt()) : 0;
                            break;
                        case "status":
                            comparison = (t1.getStatus() != null && t2.getStatus() != null) 
                                ? t1.getStatus().compareToIgnoreCase(t2.getStatus()) : 0;
                            break;
                    }
                    if (comparison != 0) {
                        return order.isAscending() ? comparison : -comparison;
                    }
                }
                return 0;
            });
        }
        
        // Aplicar paginação manual
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTeams.size());
        List<Team> paginatedTeams = start < allTeams.size() ? allTeams.subList(start, end) : new ArrayList<>();
        
        List<TeamResponse> teamResponses = paginatedTeams.stream()
            .map(team -> new TeamResponse(team, false))
            .collect(Collectors.toList());
        
        return new PageImpl<>(teamResponses, pageable, allTeams.size());
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
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.TEAMS, "refresh", null));
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
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.TEAMS, "refresh", null));
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
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.TEAMS, "refresh", null));
    }

    @Transactional
    public void removeUserFromTeam(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));
        
        if (team.getTeamUsers() != null) {
            team.getTeamUsers().removeIf(tu -> tu.getId().getUserId().equals(userId));
            teamRepository.save(team);
        }
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.TEAMS, "refresh", null));
    }

    public Page<UserResponse> findUsersByTeamId(UUID teamId, Pageable pageable) {
        // Verificar se a equipe existe
        if (!teamRepository.existsById(teamId)) {
            throw new RuntimeException("Equipe não encontrada");
        }
        
        // Buscar relacionamentos da equipe
        List<TeamUser> teamUsers = teamUserRepository.findByIdTeamId(teamId);
        
        if (teamUsers.isEmpty()) {
            // Se não houver usuários, retornar página vazia
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        // Extrair usuários dos relacionamentos
        List<com.rh360.rh360.entity.User> allUsers = teamUsers.stream()
            .map(TeamUser::getUser)
            .collect(Collectors.toList());
        
        // Aplicar ordenação se especificada
        if (pageable.getSort().isSorted()) {
            Sort sort = pageable.getSort();
            allUsers.sort((u1, u2) -> {
                for (Sort.Order order : sort) {
                    int comparison = 0;
                    String property = order.getProperty();
                    switch (property) {
                        case "name":
                            comparison = (u1.getName() != null && u2.getName() != null) 
                                ? u1.getName().compareToIgnoreCase(u2.getName()) : 0;
                            break;
                        case "email":
                            comparison = (u1.getEmail() != null && u2.getEmail() != null) 
                                ? u1.getEmail().compareToIgnoreCase(u2.getEmail()) : 0;
                            break;
                        case "role":
                            comparison = (u1.getRole() != null && u2.getRole() != null) 
                                ? u1.getRole().compareToIgnoreCase(u2.getRole()) : 0;
                            break;
                        case "status":
                            comparison = (u1.getStatus() != null && u2.getStatus() != null) 
                                ? u1.getStatus().compareToIgnoreCase(u2.getStatus()) : 0;
                            break;
                        case "createdAt":
                            comparison = (u1.getCreatedAt() != null && u2.getCreatedAt() != null) 
                                ? u1.getCreatedAt().compareTo(u2.getCreatedAt()) : 0;
                            break;
                        case "updatedAt":
                            comparison = (u1.getUpdatedAt() != null && u2.getUpdatedAt() != null) 
                                ? u1.getUpdatedAt().compareTo(u2.getUpdatedAt()) : 0;
                            break;
                    }
                    if (comparison != 0) {
                        return order.isAscending() ? comparison : -comparison;
                    }
                }
                return 0;
            });
        }
        
        // Aplicar paginação manual
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allUsers.size());
        List<com.rh360.rh360.entity.User> paginatedUsers = start < allUsers.size() 
            ? allUsers.subList(start, end) 
            : new ArrayList<>();
        
        List<UserResponse> userResponses = paginatedUsers.stream()
            .map(UserResponse::new)
            .collect(Collectors.toList());
        
        return new PageImpl<>(userResponses, pageable, allUsers.size());
    }
}
