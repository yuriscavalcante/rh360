package com.rh360.rh360.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "company_expenses")
@Data
@Schema(description = "Gasto diverso da empresa (não vinculado a um usuário)")
public class CompanyExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Schema(description = "Título do gasto", example = "Compra de materiais de escritório")
    private String title;

    @Column(nullable = false)
    @Schema(description = "Tipo do gasto", example = "material_escritorio")
    private String type;

    @Column(nullable = false)
    @Schema(description = "Data do gasto", example = "2026-02-01")
    private LocalDate date;

    @Column(precision = 19, scale = 2)
    @Schema(description = "Valor do gasto (opcional)", example = "350.00")
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Descrição (opcional)")
    private String description;

    @Schema(description = "Status", example = "paid")
    private String status;

    @Schema(description = "Data de criação do registro")
    private String createdAt;

    @Schema(description = "Data da última atualização do registro")
    private String updatedAt;

    @Schema(description = "Data de exclusão do registro (soft delete)")
    private String deletedAt;
}

