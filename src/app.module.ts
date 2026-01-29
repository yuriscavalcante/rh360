import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AuthModule } from './auth/auth.module';
import { UsersModule } from './users/users.module';
import { TeamsModule } from './teams/teams.module';
import { TimeClockModule } from './time-clock/time-clock.module';
import { PermissionsModule } from './permissions/permissions.module';
import { DatabaseModule } from './database/database.module';
import { HealthModule } from './health/health.module';
import { HelloModule } from './hello/hello.module';
import { PermissionTemplatesModule } from './permission-templates/permission-templates.module';
import { TasksModule } from './tasks/tasks.module';
import { CompanyExpensesModule } from './company-expenses/company-expenses.module';
import { FinanceExpensesModule } from './finance-expenses/finance-expenses.module';
import { FinanceSalariesModule } from './finance-salaries/finance-salaries.module';
import { FinanceSummaryModule } from './finance-summary/finance-summary.module';
import { FaceModule } from './face/face.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
    }),
    DatabaseModule,
    HealthModule,
    AuthModule,
    UsersModule,
    TeamsModule,
    TimeClockModule,
    PermissionsModule,
    HelloModule,
    PermissionTemplatesModule,
    TasksModule,
    CompanyExpensesModule,
    FinanceExpensesModule,
    FinanceSalariesModule,
    FinanceSummaryModule,
    FaceModule,
  ],
})
export class AppModule {}
