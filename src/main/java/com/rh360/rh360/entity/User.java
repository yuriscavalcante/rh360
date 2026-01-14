package com.rh360.rh360.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Schema(description = "Entidade que representa um usuário do sistema")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "ID único do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String name;

    @Column(nullable = false, unique = true)
    @Schema(description = "Email do usuário (deve ser único)", example = "joao.silva@example.com")
    private String email;

    @Schema(description = "Senha criptografada do usuário", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Schema(description = "Papel/função do usuário no sistema", example = "user")
    private String role;

    @Schema(description = "Status do usuário", example = "active")
    private String status;

    @Schema(description = "Data de criação do registro")
    private String createdAt;

    @Schema(description = "Data da última atualização do registro")
    private String updatedAt;

    @Schema(description = "Data de exclusão do registro (soft delete)")
    private String deletedAt;

}
