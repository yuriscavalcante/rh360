import { Module } from '@nestjs/common';
import { FinanceExpensesModule } from '../finance-expenses/finance-expenses.module';
import { FinanceSalariesModule } from '../finance-salaries/finance-salaries.module';
import { FinanceSummaryController } from './finance-summary.controller';
import { FinanceSummaryService } from './finance-summary.service';

@Module({
  imports: [FinanceExpensesModule, FinanceSalariesModule],
  controllers: [FinanceSummaryController],
  providers: [FinanceSummaryService],
})
export class FinanceSummaryModule {}
