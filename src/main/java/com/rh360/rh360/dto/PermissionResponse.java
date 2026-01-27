package com.rh360.rh360.dto;

import java.util.UUID;

import com.rh360.rh360.entity.Permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resposta de permissão")
public class PermissionResponse {
    
    @Schema(description = "ID da permissão", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "ID do usuário ao qual a permissão pertence", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;
    
    @Schema(description = "Função/permissão do sistema", example = "CREATE_USER")
    private String function;

    @Schema(description = "Indica se a permissão está permitida", example = "true")
    private Boolean isPermitted;

    @Schema(description = "Data de criação da permissão", example = "2021-01-01")
    private String createdAt;

    @Schema(description = "Data de atualização da permissão", example = "2021-01-01")
    private String updatedAt;

    public PermissionResponse(Permission permission) {
        this.id = permission.getId();
        this.userId = permission.getUser() != null ? permission.getUser().getId() : null;
        this.function = permission.getFunction();
        this.isPermitted = permission.getIsPermitted();
        this.createdAt = permission.getCreatedAt();
        this.updatedAt = permission.getUpdatedAt();
    }
}
