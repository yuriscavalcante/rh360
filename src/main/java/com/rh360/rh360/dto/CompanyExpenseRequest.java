package com.rh360.rh360.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Requisição para criar/atualizar gasto diverso (empresa)")
public class CompanyExpenseRequest {

    @Schema(description = "Título do gasto", example = "Compra de materiais de escritório")
    private String title;

    @Schema(description = "Tipo do gasto", example = "material_escritorio")
    private String type;

    @Schema(description = "Data do gasto", example = "2026-02-01")
    private LocalDate date;

    @Schema(description = "Valor (opcional)", example = "350.00")
    private BigDecimal amount;

    @Schema(description = "Descrição (opcional)")
    private String description;

    @Schema(description = "Status", example = "paid")
    private String status;
}

