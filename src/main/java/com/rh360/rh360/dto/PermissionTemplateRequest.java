package com.rh360.rh360.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dados de requisição para criação/atualização de template de permissão")
public class PermissionTemplateRequest {
    
    @Schema(description = "Nome do template de permissão", example = "CREATE_USER")
    private String nome;
    
    @Schema(description = "Label do template de permissão", example = "Criar Usuário")
    private String label;

    @Schema(description = "Rota associada ao template de permissão", example = "/api/users")
    private String rota;
}
