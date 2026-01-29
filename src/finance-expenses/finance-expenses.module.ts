import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Expense } from '../entities/expense.entity';
import { ExpenseAttachment } from '../entities/expense-attachment.entity';
import { User } from '../entities/user.entity';
import { FinanceExpensesController } from './finance-expenses.controller';
import { FinanceExpensesService } from './finance-expenses.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([Expense, ExpenseAttachment, User]),
  ],
  controllers: [FinanceExpensesController],
  providers: [FinanceExpensesService],
  exports: [FinanceExpensesService],
})
export class FinanceExpensesModule {}
