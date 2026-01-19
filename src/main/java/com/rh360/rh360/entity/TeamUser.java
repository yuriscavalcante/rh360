package com.rh360.rh360.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "team_users")
@Data
@Schema(description = "Entidade que representa o relacionamento entre equipe e usuário")
public class TeamUser {

    @EmbeddedId
    @Schema(description = "Chave composta do relacionamento")
    private TeamUserId id;

    @ManyToOne
    @MapsId("teamId")
    @JoinColumn(name = "team_id", nullable = false)
    @Schema(description = "Equipe relacionada")
    private Team team;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Usuário relacionado")
    private User user;

}
