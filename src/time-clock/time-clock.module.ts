import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { TimeClockController } from './time-clock.controller';
import { TimeClockService } from './time-clock.service';
import { TimeClock } from '../entities/time-clock.entity';
import { User } from '../entities/user.entity';
import { TokenModule } from '../token/token.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([TimeClock, User]),
    TokenModule,
  ],
  controllers: [TimeClockController],
  providers: [TimeClockService],
  exports: [TimeClockService],
})
export class TimeClockModule {}
