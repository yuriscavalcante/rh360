package com.rh360.rh360.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dados de permissão para inclusão em requisição de usuário")
public class UserPermissionRequest {
    
    @Schema(description = "Função/permissão do sistema", example = "CREATE_USER")
    private String function;

    @Schema(description = "Indica se a permissão está permitida", example = "true")
    private Boolean isPermitted;
}
