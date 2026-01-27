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
@Table(name = "permission_template")
@Data
@Schema(description = "Entidade que representa um template de permissão do sistema")
public class PermissionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "ID único do template de permissão", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Column(nullable = false)
    @Schema(description = "Nome do template de permissão", example = "CREATE_USER")
    private String nome;

    @Column(nullable = false)
    @Schema(description = "Label do template de permissão", example = "Criar Usuário")
    private String label;

    @Column(nullable = false)
    @Schema(description = "Rota associada ao template de permissão", example = "/api/users")
    private String rota;

    @Schema(description = "Data de criação do registro")
    private String createdAt;

    @Schema(description = "Data da última atualização do registro")
    private String updatedAt;

    @Schema(description = "Data de exclusão do registro (soft delete)")
    private String deletedAt;

}
