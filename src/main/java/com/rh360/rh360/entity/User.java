package com.rh360.rh360.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "users")
@Data
@Schema(description = "Entidade que representa um usuário do sistema")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do usuário", example = "1")
    private Long id;

    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String name;

    @Schema(description = "Email do usuário", example = "joao.silva@example.com")
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

    @Schema(description = "Usuário que criou o registro")
    private String createdBy;

    @Schema(description = "Usuário que atualizou o registro")
    private String updatedBy;

    @Schema(description = "Usuário que excluiu o registro")
    private String deletedBy;

}
