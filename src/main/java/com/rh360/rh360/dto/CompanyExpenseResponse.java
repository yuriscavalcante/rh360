package com.rh360.rh360.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.rh360.rh360.entity.CompanyExpense;
import com.rh360.rh360.entity.CompanyExpenseAttachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resposta de gasto diverso (empresa)")
public class CompanyExpenseResponse {

    private UUID id;
    private String title;
    private String type;
    private LocalDate date;
    private BigDecimal amount;
    private String description;
    private String status;
    private String createdAt;
    private String updatedAt;

    @Schema(description = "URLs dos anexos (notas fiscais/comprovantes)")
    private List<String> attachmentUrls;

    public CompanyExpenseResponse(CompanyExpense expense) {
        this.id = expense.getId();
        this.title = expense.getTitle();
        this.type = expense.getType();
        this.date = expense.getDate();
        this.amount = expense.getAmount();
        this.description = expense.getDescription();
        this.status = expense.getStatus();
        this.createdAt = expense.getCreatedAt();
        this.updatedAt = expense.getUpdatedAt();
        this.attachmentUrls = List.of();
    }

    public CompanyExpenseResponse(CompanyExpense expense, List<CompanyExpenseAttachment> attachments) {
        this(expense);
        if (attachments != null) {
            this.attachmentUrls = attachments.stream()
                    .map(CompanyExpenseAttachment::getUrl)
                    .collect(Collectors.toList());
        }
    }
}

