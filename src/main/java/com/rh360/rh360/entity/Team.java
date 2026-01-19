package com.rh360.rh360.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "teams")
@Data
@Schema(description = "Entidade que representa uma equipe do sistema")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "ID único da equipe", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Nome da equipe", example = "Equipe de Desenvolvimento")
    private String name;

    @Schema(description = "Descrição da equipe", example = "Equipe responsável pelo desenvolvimento de software")
    private String description;

    @Schema(description = "Status da equipe", example = "active")
    private String status;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Lista de relacionamentos com usuários")
    private List<TeamUser> teamUsers;

    @Schema(description = "Data de criação do registro")
    private String createdAt;

    @Schema(description = "Data da última atualização do registro")
    private String updatedAt;

    @Schema(description = "Data de exclusão do registro (soft delete)")
    private String deletedAt;

}
