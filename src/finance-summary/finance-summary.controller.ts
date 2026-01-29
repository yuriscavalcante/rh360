import {
  Controller,
  Get,
  Param,
  Query,
  UseGuards,
  HttpStatus,
  HttpException,
} from '@nestjs/common';
import { FinanceSummaryService } from './finance-summary.service';
import { FinanceSummaryResponse } from './dto/finance-summary-response.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { UserId } from '../auth/decorators/user.decorator';

@Controller('api/finance')
@UseGuards(JwtAuthGuard)
export class FinanceSummaryController {
  constructor(private readonly service: FinanceSummaryService) {}

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
