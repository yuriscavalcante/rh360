package com.rh360.rh360.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dados de requisição para criação/atualização de usuário")
public class UserRequest {
    
    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String name;
    
    @Schema(description = "Email do usuário (deve ser único)", example = "joao.silva@example.com")
    private String email;

    @Schema(description = "Senha do usuário", example = "senha123")
    private String password;

    @Schema(description = "Papel/função do usuário no sistema", example = "user")
    private String role;

    @Schema(description = "Status do usuário", example = "active")
    private String status;
}
