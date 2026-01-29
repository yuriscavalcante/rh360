import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Salary } from '../entities/salary.entity';
import { SalaryAttachment } from '../entities/salary-attachment.entity';
import { User } from '../entities/user.entity';
import { FinanceSalariesController } from './finance-salaries.controller';
import { FinanceSalariesService } from './finance-salaries.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([Salary, SalaryAttachment, User]),
  ],
  controllers: [FinanceSalariesController],
  providers: [FinanceSalariesService],
  exports: [FinanceSalariesService],
})
export class FinanceSalariesModule {}
