import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { UsersModule } from '../users/users.module';
import { FaceController } from './face.controller';
import { FaceService } from './face.service';

@Module({
  imports: [HttpModule, UsersModule],
  controllers: [FaceController],
  providers: [FaceService],
})
export class FaceModule {}
