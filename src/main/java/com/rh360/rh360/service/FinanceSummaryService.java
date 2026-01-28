package com.rh360.rh360.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rh360.rh360.dto.FinanceSummaryResponse;

@Service
public class FinanceSummaryService {

    private final SalaryService salaryService;
    private final ExpenseService expenseService;

    public FinanceSummaryService(SalaryService salaryService, ExpenseService expenseService) {
        this.salaryService = salaryService;
        this.expenseService = expenseService;
    }

    public FinanceSummaryResponse summaryForMonth(UUID userId, String referenceMonth) {
        YearMonth ym = YearMonth.parse(referenceMonth);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        BigDecimal income = salaryService.getIncomeForMonth(userId, referenceMonth);
        BigDecimal expenses = expenseService.getExpensesForMonth(userId, from, to);

        FinanceSummaryResponse resp = new FinanceSummaryResponse();
        resp.setReferenceMonth(referenceMonth);
        resp.setTotalIncome(income);
        resp.setTotalExpenses(expenses);
        resp.setBalance(income.subtract(expenses));
        return resp;
    }
}

