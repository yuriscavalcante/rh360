package com.rh360.rh360.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dados de requisição para login")
public class LoginRequest {
    
    @Schema(description = "Email do usuário", example = "usuario@example.com", required = true)
    private String email;
    
    @Schema(description = "Senha do usuário", example = "senha123", required = true)
    private String password;
}
