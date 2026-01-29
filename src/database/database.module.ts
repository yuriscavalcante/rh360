import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { User } from '../entities/user.entity';
import { Token } from '../entities/token.entity';
import { Team } from '../entities/team.entity';
import { TeamUser } from '../entities/team-user.entity';
import { TimeClock } from '../entities/time-clock.entity';
import { Permission } from '../entities/permission.entity';
import { PermissionTemplate } from '../entities/permission-template.entity';
import { Task } from '../entities/task.entity';
import { Expense } from '../entities/expense.entity';
import { ExpenseAttachment } from '../entities/expense-attachment.entity';
import { CompanyExpense } from '../entities/company-expense.entity';
import { CompanyExpenseAttachment } from '../entities/company-expense-attachment.entity';
import { Salary } from '../entities/salary.entity';
import { SalaryAttachment } from '../entities/salary-attachment.entity';
import { QrCodeToken } from '../entities/qrcode-token.entity';

@Module({
  imports: [
    TypeOrmModule.forRootAsync({
      imports: [ConfigModule],
      useFactory: (configService: ConfigService) => ({
        type: 'postgres',
        host: configService.get('DB_HOST', 'localhost'),
        port: configService.get<number>('DB_PORT', 5432),
        username: configService.get('DB_USERNAME', 'postgres'),
        password: configService.get('DB_PASSWORD', 'postgres'),
        database: configService.get('DB_DATABASE', 'rh360'),
        entities: [
          User,
          Token,
          Team,
          TeamUser,
          TimeClock,
          Permission,
          PermissionTemplate,
          Task,
          Expense,
          ExpenseAttachment,
          CompanyExpense,
          CompanyExpenseAttachment,
          Salary,
          SalaryAttachment,
          QrCodeToken,
        ],
        synchronize: false,
        logging: configService.get('DB_LOGGING', 'false') === 'true',
      }),
      inject: [ConfigService],
    }),
  ],
})
export class DatabaseModule {}
