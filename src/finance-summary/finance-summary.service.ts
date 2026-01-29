import { Injectable } from '@nestjs/common';
import { FinanceExpensesService } from '../finance-expenses/finance-expenses.service';
import { FinanceSalariesService } from '../finance-salaries/finance-salaries.service';
import { FinanceSummaryResponse } from './dto/finance-summary-response.dto';

@Injectable()
export class FinanceSummaryService {
  constructor(
    private readonly salaryService: FinanceSalariesService,
    private readonly expenseService: FinanceExpensesService,
  ) {}

  async summaryForMonth(
    userId: string,
    referenceMonth: string,
  ): Promise<FinanceSummaryResponse> {
    // Parse referenceMonth (YYYY-MM) to get first and last day
    const [year, month] = referenceMonth.split('-').map(Number);
    const from = new Date(year, month - 1, 1);
    const to = new Date(year, month, 0); // Last day of month

    const income = await this.salaryService.getIncomeForMonth(userId, referenceMonth);
    const expenses = await this.expenseService.getExpensesForMonth(userId, from, to);

    return {
      referenceMonth,
      totalIncome: income,
      totalExpenses: expenses,
      balance: income - expenses,
    };
  }
}
