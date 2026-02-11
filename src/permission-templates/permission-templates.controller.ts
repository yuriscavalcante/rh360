import {
  Controller,
  Get,
  Post,
  Body,
  Query,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse, ApiQuery } from '@nestjs/swagger';
import { PermissionTemplatesService } from './permission-templates.service';
import { PermissionTemplateRequest } from './dto/permission-template-request.dto';
import { PermissionTemplateResponse } from './dto/permission-template-response.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';

@ApiTags('Templates de Permissão')
@Controller('api/permission-templates')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class PermissionTemplatesController {
  constructor(private readonly service: PermissionTemplatesService) {}

  @ApiOperation({ summary: 'Criar template de permissão' })
  @ApiResponse({ status: 201, type: PermissionTemplateResponse })
  @Post()
  async create(
    @Body() request: PermissionTemplateRequest,
  ): Promise<PermissionTemplateResponse> {
    return this.service.create(request);
  }

  @ApiOperation({ summary: 'Listar templates de permissão' })
  @ApiQuery({ name: 'search', required: false })
  @ApiResponse({ status: 200, type: [PermissionTemplateResponse] })
  @Get()
  async findAll(
    @Query('search') search?: string,
  ): Promise<PermissionTemplateResponse[]> {
    return this.service.findAll(search);
  }
}
