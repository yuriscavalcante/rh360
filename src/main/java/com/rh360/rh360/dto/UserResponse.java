package com.rh360.rh360.dto;

import java.util.UUID;

import com.rh360.rh360.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resposta de usuário")
public class UserResponse {
    
    @Schema(description = "ID do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "Nome do usuário", example = "João da Silva")
    private String name;
    
    @Schema(description = "Email do usuário", example = "joao.silva@example.com")
    private String email;

    @Schema(description = "Role do usuário", example = "admin")
    private String role;

    @Schema(description = "Status do usuário", example = "active")
    private String status;

    @Schema(description = "Data de criação do usuário", example = "2021-01-01")
    private String createdAt;

    @Schema(description = "Data de atualização do usuário", example = "2021-01-01")
    private String updatedAt;

    @Schema(description = "URL da foto do usuário", example = "https://pub-xxx.r2.dev/users/photo.jpg")
    private String photo;

    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.photo = user.getPhoto();
    }
}
