import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Expense } from '../entities/expense.entity';
import { ExpenseAttachment } from '../entities/expense-attachment.entity';
import { User } from '../entities/user.entity';
import { TokenModule } from '../token/token.module';
import { FinanceExpensesController } from './finance-expenses.controller';
import { FinanceExpensesService } from './finance-expenses.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([Expense, ExpenseAttachment, User]),
    TokenModule,
  ],
  controllers: [FinanceExpensesController],
  providers: [FinanceExpensesService],
  exports: [FinanceExpensesService],
})
export class FinanceExpensesModule {}
