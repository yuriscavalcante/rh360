import {
  Controller,
  Get,
  Post,
  Body,
  Param,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { UserId } from '../auth/decorators/user.decorator';
import { PermissionsService } from './permissions.service';
import { Permission } from '../entities/permission.entity';
import { PermissionTemplate } from '../entities/permission-template.entity';

@ApiTags('Permissões')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('api/permissions')
export class PermissionsController {
  constructor(private readonly permissionsService: PermissionsService) {}

  @ApiOperation({ summary: 'Listar permissões do usuário atual' })
  @Get('me')
  async findByUser(@UserId() userId: string): Promise<Permission[]> {
    return this.permissionsService.findByUser(userId);
  }

  @ApiOperation({ summary: 'Listar templates de permissão' })
  @Get('templates')
  async findAllTemplates(): Promise<PermissionTemplate[]> {
    return this.permissionsService.findAllTemplates();
  }

  @ApiOperation({ summary: 'Criar template de permissão' })
  @Post('templates')
  async createTemplate(
    @Body() templateData: Partial<PermissionTemplate>,
  ): Promise<PermissionTemplate> {
    return this.permissionsService.createTemplate(templateData);
  }
}
