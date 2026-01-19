package com.rh360.rh360.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Requisição para criar ou atualizar uma equipe")
public class TeamRequest {
    
    @Schema(description = "Nome da equipe", example = "Equipe de Desenvolvimento")
    private String name;
    
    @Schema(description = "Descrição da equipe", example = "Equipe responsável pelo desenvolvimento de software")
    private String description;
    
    @Schema(description = "Lista de IDs dos usuários a serem associados à equipe", example = "[\"550e8400-e29b-41d4-a716-446655440000\"]")
    private List<UUID> userIds;
    
    @Schema(description = "Lista de IDs dos usuários a serem associados à equipe (alias para userIds)", example = "[\"550e8400-e29b-41d4-a716-446655440000\"]")
    private List<UUID> users;
}
