package com.rh360.rh360.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.rh360.rh360.entity.Expense;
import com.rh360.rh360.entity.ExpenseAttachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resposta de despesa")
public class ExpenseResponse {

    @Schema(description = "ID da despesa")
    private UUID id;

    @Schema(description = "ID do usuário dono da despesa")
    private UUID userId;

    @Schema(description = "Data")
    private LocalDate date;

    @Schema(description = "Valor")
    private BigDecimal amount;

    @Schema(description = "Descrição")
    private String description;

    @Schema(description = "Categoria")
    private String category;

    @Schema(description = "Forma de pagamento")
    private String paymentMethod;

    @Schema(description = "Fornecedor")
    private String vendor;

    @Schema(description = "Status")
    private String status;

    @Schema(description = "Criado em")
    private String createdAt;

    @Schema(description = "Atualizado em")
    private String updatedAt;

    @Schema(description = "URLs dos anexos (notas fiscais/comprovantes)")
    private List<String> attachmentUrls;

    public ExpenseResponse(Expense expense) {
        this.id = expense.getId();
        this.userId = expense.getUser() != null ? expense.getUser().getId() : null;
        this.date = expense.getDate();
        this.amount = expense.getAmount();
        this.description = expense.getDescription();
        this.category = expense.getCategory();
        this.paymentMethod = expense.getPaymentMethod();
        this.vendor = expense.getVendor();
        this.status = expense.getStatus();
        this.createdAt = expense.getCreatedAt();
        this.updatedAt = expense.getUpdatedAt();
        this.attachmentUrls = List.of();
    }

    public ExpenseResponse(Expense expense, List<ExpenseAttachment> attachments) {
        this(expense);
        if (attachments != null) {
            this.attachmentUrls = attachments.stream()
                    .map(ExpenseAttachment::getUrl)
                    .collect(Collectors.toList());
        }
    }
}

