import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { JwtModule } from '@nestjs/jwt';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { QrCodeTokenService } from './qrcode-token.service';
import { QrCodeToken } from '../entities/qrcode-token.entity';

@Module({
  imports: [
    TypeOrmModule.forFeature([QrCodeToken]),
    JwtModule.registerAsync({
      imports: [ConfigModule],
      useFactory: async (configService: ConfigService) => ({
        secret: configService.get<string>('JWT_SECRET'),
      }),
      inject: [ConfigService],
    }),
  ],
  providers: [QrCodeTokenService],
  exports: [QrCodeTokenService],
})
export class QrCodeTokenModule {}
