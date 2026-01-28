package com.rh360.rh360.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Requisição para criar/atualizar despesa")
public class ExpenseRequest {

    @Schema(description = "ID do usuário (obrigatório para endpoints admin). Em endpoints /me pode ser omitido.",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "Data do gasto", example = "2026-01-10")
    private LocalDate date;

    @Schema(description = "Valor do gasto", example = "120.50")
    private BigDecimal amount;

    @Schema(description = "Descrição do gasto", example = "Almoço com cliente")
    private String description;

    @Schema(description = "Categoria (opcional)", example = "alimentacao")
    private String category;

    @Schema(description = "Forma de pagamento (opcional)", example = "cartao")
    private String paymentMethod;

    @Schema(description = "Fornecedor (opcional)", example = "Restaurante X")
    private String vendor;

    @Schema(description = "Status", example = "paid")
    private String status;
}

