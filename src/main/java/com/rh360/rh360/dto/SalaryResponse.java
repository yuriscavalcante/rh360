package com.rh360.rh360.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.rh360.rh360.entity.Salary;
import com.rh360.rh360.entity.SalaryAttachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resposta de salário")
public class SalaryResponse {

    @Schema(description = "ID do salário")
    private UUID id;

    @Schema(description = "ID do usuário dono do salário")
    private UUID userId;

    @Schema(description = "Mês de referência (YYYY-MM)")
    private String referenceMonth;

    @Schema(description = "Valor bruto")
    private BigDecimal grossAmount;

    @Schema(description = "Valor líquido")
    private BigDecimal netAmount;

    @Schema(description = "Descontos")
    private BigDecimal discounts;

    @Schema(description = "Bônus")
    private BigDecimal bonuses;

    @Schema(description = "Data de pagamento")
    private LocalDate paidAt;

    @Schema(description = "Observações")
    private String notes;

    @Schema(description = "Status")
    private String status;

    @Schema(description = "Criado em")
    private String createdAt;

    @Schema(description = "Atualizado em")
    private String updatedAt;

    @Schema(description = "URLs dos anexos (contracheques/comprovantes)")
    private List<String> attachmentUrls;

    public SalaryResponse(Salary salary) {
        this.id = salary.getId();
        this.userId = salary.getUser() != null ? salary.getUser().getId() : null;
        this.referenceMonth = salary.getReferenceMonth();
        this.grossAmount = salary.getGrossAmount();
        this.netAmount = salary.getNetAmount();
        this.discounts = salary.getDiscounts();
        this.bonuses = salary.getBonuses();
        this.paidAt = salary.getPaidAt();
        this.notes = salary.getNotes();
        this.status = salary.getStatus();
        this.createdAt = salary.getCreatedAt();
        this.updatedAt = salary.getUpdatedAt();
        this.attachmentUrls = List.of();
    }

    public SalaryResponse(Salary salary, List<SalaryAttachment> attachments) {
        this(salary);
        if (attachments != null) {
            this.attachmentUrls = attachments.stream()
                    .map(SalaryAttachment::getUrl)
                    .collect(Collectors.toList());
        }
    }
}

