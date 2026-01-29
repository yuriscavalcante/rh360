import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CompanyExpense } from '../entities/company-expense.entity';
import { CompanyExpenseAttachment } from '../entities/company-expense-attachment.entity';
import { CompanyExpensesController } from './company-expenses.controller';
import { CompanyExpensesService } from './company-expenses.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([CompanyExpense, CompanyExpenseAttachment]),
  ],
  controllers: [CompanyExpensesController],
  providers: [CompanyExpensesService],
})
export class CompanyExpensesModule {}
