package com.rh360.rh360.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resumo financeiro (salários x despesas)")
public class FinanceSummaryResponse {

    @Schema(description = "Mês de referência (YYYY-MM)", example = "2026-01")
    private String referenceMonth;

    @Schema(description = "Total de salários (líquido se informado, senão bruto)", example = "4200.00")
    private BigDecimal totalIncome;

    @Schema(description = "Total de despesas", example = "1250.00")
    private BigDecimal totalExpenses;

    @Schema(description = "Saldo do mês (income - expenses)", example = "2950.00")
    private BigDecimal balance;
}

