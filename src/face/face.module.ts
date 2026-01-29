import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { UsersModule } from '../users/users.module';
import { TokenModule } from '../token/token.module';
import { FaceController } from './face.controller';
import { FaceService } from './face.service';

@Module({
  imports: [
    HttpModule.register({}),
    UsersModule,
    TokenModule,
  ],
  controllers: [FaceController],
  providers: [FaceService],
})
export class FaceModule {}
