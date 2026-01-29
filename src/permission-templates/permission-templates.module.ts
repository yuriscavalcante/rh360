import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PermissionTemplate } from '../entities/permission-template.entity';
import { PermissionTemplatesController } from './permission-templates.controller';
import { PermissionTemplatesService } from './permission-templates.service';

@Module({
  imports: [TypeOrmModule.forFeature([PermissionTemplate])],
  controllers: [PermissionTemplatesController],
  providers: [PermissionTemplatesService],
})
export class PermissionTemplatesModule {}
