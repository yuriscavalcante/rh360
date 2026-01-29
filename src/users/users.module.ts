import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { UsersController } from './users.controller';
import { UsersService } from './users.service';
import { UsersGateway } from './users.gateway';
import { User } from '../entities/user.entity';
import { Permission } from '../entities/permission.entity';
import { TokenModule } from '../token/token.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([User, Permission]),
    TokenModule,
  ],
  controllers: [UsersController],
  providers: [UsersService, UsersGateway],
  exports: [UsersService, UsersGateway],
})
export class UsersModule {}
