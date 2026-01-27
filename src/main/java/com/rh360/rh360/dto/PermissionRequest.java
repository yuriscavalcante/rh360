package com.rh360.rh360.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.UUID;

@Data
@Schema(description = "Dados de requisição para criação/atualização de permissão")
public class PermissionRequest {
    
    @Schema(description = "ID do usuário ao qual a permissão pertence", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;
    
    @Schema(description = "Função/permissão do sistema", example = "CREATE_USER")
    private String function;

    @Schema(description = "Indica se a permissão está permitida", example = "true")
    private Boolean isPermitted;
}
