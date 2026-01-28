package com.rh360.rh360.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Requisição para criar/atualizar salário")
public class SalaryRequest {

    @Schema(description = "ID do usuário (obrigatório para endpoints admin). Em endpoints /me pode ser omitido.",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "Mês de referência no formato YYYY-MM", example = "2026-01")
    private String referenceMonth;

    @Schema(description = "Valor bruto", example = "5000.00")
    private BigDecimal grossAmount;

    @Schema(description = "Valor líquido (opcional)", example = "4200.00")
    private BigDecimal netAmount;

    @Schema(description = "Descontos (opcional)", example = "800.00")
    private BigDecimal discounts;

    @Schema(description = "Bônus/proventos extras (opcional)", example = "150.00")
    private BigDecimal bonuses;

    @Schema(description = "Data de pagamento (opcional)", example = "2026-01-31")
    private LocalDate paidAt;

    @Schema(description = "Observações (opcional)")
    private String notes;

    @Schema(description = "Status", example = "paid")
    private String status;
}

