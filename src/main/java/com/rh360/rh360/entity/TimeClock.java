package com.rh360.rh360.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_clocks")
@Data
@Schema(description = "Entidade que representa um registro de ponto (bater ponto)")
public class TimeClock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "ID único do registro de ponto", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Usuário que bateu o ponto")
    private User user;

    @Column(nullable = false)
    @Schema(description = "Data e hora do registro de ponto", example = "2024-01-15T08:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Data de criação do registro")
    private String createdAt;

    @Schema(description = "Data da última atualização do registro")
    private String updatedAt;

}
