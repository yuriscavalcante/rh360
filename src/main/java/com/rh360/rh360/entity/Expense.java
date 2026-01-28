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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "expenses")
@Data
@Schema(description = "Entidade que representa um gasto/despesa diversa")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "ID único da despesa", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Usuário dono do lançamento")
    private User user;

    @Column(nullable = false)
    @Schema(description = "Data do gasto", example = "2026-01-10")
    private LocalDate date;

    @Column(nullable = false, precision = 19, scale = 2)
    @Schema(description = "Valor do gasto", example = "120.50")
    private BigDecimal amount;

    @Column(nullable = false)
    @Schema(description = "Descrição do gasto", example = "Almoço com cliente")
    private String description;

    @Schema(description = "Categoria do gasto (opcional)", example = "alimentacao")
    private String category;

    @Column(name = "payment_method")
    @Schema(description = "Forma de pagamento (opcional)", example = "cartao")
    private String paymentMethod;

    @Schema(description = "Fornecedor/estabelecimento (opcional)", example = "Restaurante X")
    private String vendor;

    @Schema(description = "Status do gasto", example = "paid")
    private String status;

    @Schema(description = "Data de criação do registro")
    private String createdAt;

    @Schema(description = "Data da última atualização do registro")
    private String updatedAt;

    @Schema(description = "Data de exclusão do registro (soft delete)")
    private String deletedAt;
}

