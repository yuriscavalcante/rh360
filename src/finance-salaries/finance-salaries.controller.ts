import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  Query,
  UseGuards,
  UseInterceptors,
  UploadedFiles,
  HttpStatus,
  HttpException,
} from '@nestjs/common';
import { FilesInterceptor } from '@nestjs/platform-express';
import { FinanceSalariesService } from './finance-salaries.service';
import { SalaryRequest } from './dto/salary-request.dto';
import { SalaryResponse } from './dto/salary-response.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { UserId } from '../auth/decorators/user.decorator';
import { DateUtil } from '../utils/date.util';

@Controller('api/finance/salaries')
@UseGuards(JwtAuthGuard)
export class FinanceSalariesController {
  constructor(private readonly service: FinanceSalariesService) {}

  @Post('me')
  @UseInterceptors(FilesInterceptor('files'))
  async createMe(
    @Body() body: any,
    @UserId() userId: string,
    @UploadedFiles() files?: Express.Multer.File[],
  ): Promise<SalaryResponse> {
    try {
      const request: SalaryRequest = {
        referenceMonth: body.referenceMonth,
        grossAmount: body.grossAmount ? parseFloat(body.grossAmount) : undefined,
        netAmount: body.netAmount ? parseFloat(body.netAmount) : undefined,
        discounts: body.discounts ? parseFloat(body.discounts) : undefined,
        bonuses: body.bonuses ? parseFloat(body.bonuses) : undefined,
        paidAt: body.paidAt,
        notes: body.notes,
        status: body.status,
      };
      if (request.paidAt) {
        request.paidAt = DateUtil.parseFlexibleDate(request.paidAt)?.toISOString() || request.paidAt;
      }
      return this.service.create(userId, request, files);
    } catch (e) {
      throw new HttpException(
        { error: e.message },
        HttpStatus.BAD_REQUEST,
      );
    }
  }

  @Get('me')
  async listMe(
    @UserId() userId: string,
    @Query('referenceMonth') referenceMonth?: string,
    @Query('fromPaidAt') fromPaidAt?: string,
    @Query('toPaidAt') toPaidAt?: string,
  ): Promise<SalaryResponse[]> {
    const fromPaidAtDate = fromPaidAt ? DateUtil.parseFlexibleDate(fromPaidAt) : undefined;
    const toPaidAtDate = toPaidAt ? DateUtil.parseFlexibleDate(toPaidAt) : undefined;
    return this.service.list(userId, referenceMonth, fromPaidAtDate, toPaidAtDate);
  }

  @Get('me/:salaryId')
  async findByIdMe(
    @Param('salaryId') salaryId: string,
    @UserId() userId: string,
  ): Promise<SalaryResponse> {
    return this.service.findById(salaryId, userId);
  }

  @Put('me/:salaryId')
  @UseInterceptors(FilesInterceptor('files'))
  async updateMe(
    @Param('salaryId') salaryId: string,
    @Body() body: any,
    @UserId() userId: string,
    @UploadedFiles() files?: Express.Multer.File[],
  ): Promise<SalaryResponse> {
    try {
      const request: SalaryRequest = {
        referenceMonth: body.referenceMonth,
        grossAmount: body.grossAmount ? parseFloat(body.grossAmount) : undefined,
        netAmount: body.netAmount ? parseFloat(body.netAmount) : undefined,
        discounts: body.discounts ? parseFloat(body.discounts) : undefined,
        bonuses: body.bonuses ? parseFloat(body.bonuses) : undefined,
        paidAt: body.paidAt,
        notes: body.notes,
        status: body.status,
      };
      if (request.paidAt) {
        request.paidAt = DateUtil.parseFlexibleDate(request.paidAt)?.toISOString() || request.paidAt;
      }
      return this.service.update(salaryId, userId, request, files);
    } catch (e) {
      throw new HttpException(
        { error: e.message },
        HttpStatus.BAD_REQUEST,
      );
    }
  }

  @Delete('me/:salaryId')
  async deleteMe(
    @Param('salaryId') salaryId: string,
    @UserId() userId: string,
  ): Promise<{ success: boolean }> {
    try {
      await this.service.delete(salaryId, userId);
      return { success: true };
    } catch (e) {
      throw new HttpException(
        { error: e.message },
        HttpStatus.BAD_REQUEST,
      );
    }
  }

  @Post('users/:userId')
  @UseInterceptors(FilesInterceptor('files'))
  async createForUser(
    @Param('userId') userId: string,
    @Body() body: any,
    @UploadedFiles() files?: Express.Multer.File[],
  ): Promise<SalaryResponse> {
    try {
      const request: SalaryRequest = {
        referenceMonth: body.referenceMonth,
        grossAmount: body.grossAmount ? parseFloat(body.grossAmount) : undefined,
        netAmount: body.netAmount ? parseFloat(body.netAmount) : undefined,
        discounts: body.discounts ? parseFloat(body.discounts) : undefined,
        bonuses: body.bonuses ? parseFloat(body.bonuses) : undefined,
        paidAt: body.paidAt,
        notes: body.notes,
        status: body.status,
      };
      if (request.paidAt) {
        request.paidAt = DateUtil.parseFlexibleDate(request.paidAt)?.toISOString() || request.paidAt;
      }
      return this.service.create(userId, request, files);
    } catch (e) {
      throw new HttpException(
        { error: e.message },
        HttpStatus.BAD_REQUEST,
      );
    }
  }

  @Get('users/:userId')
  async listForUser(
    @Param('userId') userId: string,
    @Query('referenceMonth') referenceMonth?: string,
    @Query('fromPaidAt') fromPaidAt?: string,
    @Query('toPaidAt') toPaidAt?: string,
  ): Promise<SalaryResponse[]> {
    const fromPaidAtDate = fromPaidAt ? DateUtil.parseFlexibleDate(fromPaidAt) : undefined;
    const toPaidAtDate = toPaidAt ? DateUtil.parseFlexibleDate(toPaidAt) : undefined;
    return this.service.list(userId, referenceMonth, fromPaidAtDate, toPaidAtDate);
  }
}
