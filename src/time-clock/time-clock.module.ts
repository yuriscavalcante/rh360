import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { TimeClockController } from './time-clock.controller';
import { TimeClockService } from './time-clock.service';
import { QrCodeService } from './qrcode.service';
import { TimeClock } from '../entities/time-clock.entity';
import { User } from '../entities/user.entity';
import { TokenModule } from '../token/token.module';
import { QrCodeTokenModule } from '../qrcode-token/qrcode-token.module';
import { QrCodeTokenGuard } from '../auth/guards/qrcode-token.guard';
import { FaceModule } from '../face/face.module';
import { UsersModule } from '../users/users.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([TimeClock, User]),
    TokenModule,
    QrCodeTokenModule,
    FaceModule,
    UsersModule,
  ],
  controllers: [TimeClockController],
  providers: [TimeClockService, QrCodeService, QrCodeTokenGuard],
  exports: [TimeClockService],
})
export class TimeClockModule {}
