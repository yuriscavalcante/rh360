package com.rh360.rh360.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resposta de login contendo o token JWT")
public class LoginResponse {
    
    @Schema(description = "ID do usuário autenticado", example = "1")
    private Long id;
    
    @Schema(description = "Token JWT para autenticação", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    public LoginResponse(Long id, String token) {
        this.id = id;
        this.token = token;
    }
}
