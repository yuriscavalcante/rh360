import {
  Controller,
  Get,
  Post,
  Body,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { UserId } from '../auth/decorators/user.decorator';
import { TimeClockService } from './time-clock.service';
import { TimeClock } from '../entities/time-clock.entity';

@ApiTags('Ponto')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('api/time-clock')
export class TimeClockController {
  constructor(private readonly timeClockService: TimeClockService) {}

  @ApiOperation({ summary: 'Bater ponto' })
  @Post()
  async create(
    @UserId() userId: string,
    @Body('message') message?: string,
  ): Promise<TimeClock> {
    return this.timeClockService.create(userId, message);
  }

  @ApiOperation({ summary: 'Listar registros de ponto do usuário atual' })
  @Get('me')
  async findByUser(@UserId() userId: string): Promise<TimeClock[]> {
    return this.timeClockService.findByUser(userId);
  }

  @ApiOperation({ summary: 'Listar todos os registros de ponto' })
  @Get()
  async findAll(): Promise<TimeClock[]> {
    return this.timeClockService.findAll();
  }
}
