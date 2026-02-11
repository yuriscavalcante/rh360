import {
  Controller,
  Get,
  Param,
  Query,
  UseGuards,
  HttpStatus,
  HttpException,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse, ApiQuery, ApiParam } from '@nestjs/swagger';
import { FinanceSummaryService } from './finance-summary.service';
import { FinanceSummaryResponse } from './dto/finance-summary-response.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { UserId } from '../auth/decorators/user.decorator';

@ApiTags('Financeiro - Resumo')
@Controller('api/finance')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class FinanceSummaryController {
  constructor(private readonly service: FinanceSummaryService) {}

  @ApiOperation({ summary: 'Resumo financeiro do usuário logado' })
  @ApiQuery({ name: 'referenceMonth', description: 'Mês de referência (ex: 2025-01)' })
  @ApiResponse({ status: 200, description: 'Resumo do mês', type: FinanceSummaryResponse })
  @Get('summary/me')
  async summaryMe(
    @Query('referenceMonth') referenceMonth: string,
    @UserId() userId: string,
  ): Promise<FinanceSummaryResponse> {
    if (!referenceMonth) {
      throw new HttpException(
        { error: 'referenceMonth é obrigatório' },
        HttpStatus.BAD_REQUEST,
      );
    }
    try {
      return this.service.summaryForMonth(userId, referenceMonth);
    } catch (e) {
      throw new HttpException(
        { error: e.message },
        HttpStatus.BAD_REQUEST,
      );
    }
  }

  @ApiOperation({ summary: 'Resumo financeiro por usuário' })
  @ApiParam({ name: 'userId', description: 'ID do usuário' })
  @ApiQuery({ name: 'referenceMonth', description: 'Mês de referência' })
  @ApiResponse({ status: 200, type: FinanceSummaryResponse })
  @Get('summary/users/:userId')
  async summaryForUser(
    @Param('userId') userId: string,
    @Query('referenceMonth') referenceMonth: string,
  ): Promise<FinanceSummaryResponse> {
    if (!referenceMonth) {
      throw new HttpException(
        { error: 'referenceMonth é obrigatório' },
        HttpStatus.BAD_REQUEST,
      );
    }
    try {
      return this.service.summaryForMonth(userId, referenceMonth);
    } catch (e) {
      throw new HttpException(
        { error: e.message },
        HttpStatus.BAD_REQUEST,
      );
    }
  }
}
