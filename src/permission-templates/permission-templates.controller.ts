import {
  Controller,
  Get,
  Post,
  Body,
  Query,
  UseGuards,
} from '@nestjs/common';
import { PermissionTemplatesService } from './permission-templates.service';
import { PermissionTemplateRequest } from './dto/permission-template-request.dto';
import { PermissionTemplateResponse } from './dto/permission-template-response.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';

@Controller('api/permission-templates')
@UseGuards(JwtAuthGuard)
export class PermissionTemplatesController {
  constructor(private readonly service: PermissionTemplatesService) {}

  @Post()
  async create(
    @Body() request: PermissionTemplateRequest,
  ): Promise<PermissionTemplateResponse> {
    return this.service.create(request);
  }

  @Get()
  async findAll(
    @Query('search') search?: string,
  ): Promise<PermissionTemplateResponse[]> {
    return this.service.findAll(search);
  }
}
