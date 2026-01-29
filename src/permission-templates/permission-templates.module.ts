import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PermissionTemplate } from '../entities/permission-template.entity';
import { TokenModule } from '../token/token.module';
import { PermissionTemplatesController } from './permission-templates.controller';
import { PermissionTemplatesService } from './permission-templates.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([PermissionTemplate]),
    TokenModule,
  ],
  controllers: [PermissionTemplatesController],
  providers: [PermissionTemplatesService],
})
export class PermissionTemplatesModule {}
