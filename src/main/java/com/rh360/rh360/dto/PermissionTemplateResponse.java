package com.rh360.rh360.dto;

import java.util.UUID;

import com.rh360.rh360.entity.PermissionTemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resposta de template de permissão")
public class PermissionTemplateResponse {
    
    @Schema(description = "ID do template de permissão", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "Nome do template de permissão", example = "CREATE_USER")
    private String nome;

    @Schema(description = "Label do template de permissão", example = "Criar Usuário")
    private String label;

    @Schema(description = "Rota associada ao template de permissão", example = "/api/users")
    private String rota;

    @Schema(description = "Data de criação do template", example = "2021-01-01")
    private String createdAt;

    @Schema(description = "Data de atualização do template", example = "2021-01-01")
    private String updatedAt;

    public PermissionTemplateResponse(PermissionTemplate permissionTemplate) {
        this.id = permissionTemplate.getId();
        this.nome = permissionTemplate.getNome();
        this.label = permissionTemplate.getLabel();
        this.rota = permissionTemplate.getRota();
        this.createdAt = permissionTemplate.getCreatedAt();
        this.updatedAt = permissionTemplate.getUpdatedAt();
    }
}
