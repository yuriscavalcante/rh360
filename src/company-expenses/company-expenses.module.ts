import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CompanyExpense } from '../entities/company-expense.entity';
import { CompanyExpenseAttachment } from '../entities/company-expense-attachment.entity';
import { TokenModule } from '../token/token.module';
import { CompanyExpensesController } from './company-expenses.controller';
import { CompanyExpensesService } from './company-expenses.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([CompanyExpense, CompanyExpenseAttachment]),
    TokenModule,
  ],
  controllers: [CompanyExpensesController],
  providers: [CompanyExpensesService],
})
export class CompanyExpensesModule {}
