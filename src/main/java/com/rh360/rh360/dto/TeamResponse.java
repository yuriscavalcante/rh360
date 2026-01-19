package com.rh360.rh360.dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.rh360.rh360.entity.Team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resposta de equipe")
public class TeamResponse {
    
    @Schema(description = "ID da equipe", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "Nome da equipe", example = "Equipe de Desenvolvimento")
    private String name;
    
    @Schema(description = "Descrição da equipe", example = "Equipe responsável pelo desenvolvimento de software")
    private String description;
    
    @Schema(description = "Status da equipe", example = "active")
    private String status;
    
    @Schema(description = "Lista de usuários associados à equipe")
    private List<UserResponse> users;
    
    @Schema(description = "Data de criação da equipe", example = "2021-01-01")
    private String createdAt;
    
    @Schema(description = "Data de atualização da equipe", example = "2021-01-01")
    private String updatedAt;

    public TeamResponse(Team team) {
        this(team, false);
    }
    
    public TeamResponse(Team team, boolean includeUsers) {
        this.id = team.getId();
        this.name = team.getName();
        this.description = team.getDescription();
        this.status = team.getStatus();
        this.createdAt = team.getCreatedAt();
        this.updatedAt = team.getUpdatedAt();
        
        if (includeUsers && team.getTeamUsers() != null && !team.getTeamUsers().isEmpty()) {
            this.users = team.getTeamUsers().stream()
                .map(tu -> new UserResponse(tu.getUser()))
                .collect(Collectors.toList());
        } else {
            this.users = List.of();
        }
    }
}
