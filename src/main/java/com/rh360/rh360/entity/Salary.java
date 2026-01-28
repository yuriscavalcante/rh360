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
@Table(name = "salaries")
@Data
@Schema(description = "Entidade que representa um registro de salário/pagamento")
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "ID único do salário", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Usuário do salário")
    private User user;

    @Column(name = "reference_month", nullable = false)
    @Schema(description = "Mês de referência no formato YYYY-MM", example = "2026-01")
    private String referenceMonth;

    @Column(name = "gross_amount", nullable = false, precision = 19, scale = 2)
    @Schema(description = "Valor bruto", example = "5000.00")
    private BigDecimal grossAmount;

    @Column(name = "net_amount", precision = 19, scale = 2)
    @Schema(description = "Valor líquido (opcional)", example = "4200.00")
    private BigDecimal netAmount;

    @Column(name = "discounts", precision = 19, scale = 2)
    @Schema(description = "Total de descontos (opcional)", example = "800.00")
    private BigDecimal discounts;

    @Column(name = "bonuses", precision = 19, scale = 2)
    @Schema(description = "Total de bônus/proventos extras (opcional)", example = "150.00")
    private BigDecimal bonuses;

    @Column(name = "paid_at")
    @Schema(description = "Data de pagamento (opcional)", example = "2026-01-31")
    private LocalDate paidAt;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Observações (opcional)")
    private String notes;

    @Schema(description = "Status do salário", example = "paid")
    private String status;

    @Schema(description = "Data de criação do registro")
    private String createdAt;

    @Schema(description = "Data da última atualização do registro")
    private String updatedAt;

    @Schema(description = "Data de exclusão do registro (soft delete)")
    private String deletedAt;
}

