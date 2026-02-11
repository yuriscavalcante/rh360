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
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse } from '@nestjs/swagger';
import { FinanceExpensesService } from './finance-expenses.service';
import { ExpenseRequest } from './dto/expense-request.dto';
import { ExpenseResponse } from './dto/expense-response.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { UserId } from '../auth/decorators/user.decorator';
import { DateUtil } from '../utils/date.util';

@ApiTags('Financeiro - Despesas')
@Controller('api/finance/expenses')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class FinanceExpensesController {
  constructor(private readonly service: FinanceExpensesService) {}

  @Post('me')
  @UseInterceptors(FilesInterceptor('files'))
  async createMe(
    @Body() body: any,
    @UserId() userId: string,
    @UploadedFiles() files?: Express.Multer.File[],
  ): Promise<ExpenseResponse> {
    // Se tem files, é multipart, senão é JSON
    if (files && files.length > 0) {
      try {
        const request: ExpenseRequest = {
          date: body.date,
          amount: body.amount ? parseFloat(body.amount) : undefined,
          description: body.description,
          category: body.category,
          paymentMethod: body.paymentMethod,
          vendor: body.vendor,
          status: body.status,
        };
        if (request.date) {
          request.date = DateUtil.parseFlexibleDate(request.date)?.toISOString() || request.date;
        }
        return this.service.create(userId, request, files);
      } catch (e) {
        throw new HttpException(
          { error: e.message },
          HttpStatus.BAD_REQUEST,
        );
      }
    } else {
      // JSON request
      const request = body as ExpenseRequest;
      if (request.date && typeof request.date === 'string') {
        const parsed = DateUtil.parseFlexibleDate(request.date);
        if (parsed) {
          request.date = parsed.toISOString();
        }
      }
      return this.service.create(userId, request);
    }
  }

  @Post('me/multipart')
  @UseInterceptors(FilesInterceptor('files'))
  async createMeMultipart(
    @Body() body: any,
    @UserId() userId: string,
    @UploadedFiles() files?: Express.Multer.File[],
  ): Promise<ExpenseResponse> {
    try {
      const request: ExpenseRequest = {
        date: body.date,
        amount: body.amount ? parseFloat(body.amount) : undefined,
        description: body.description,
        category: body.category,
        paymentMethod: body.paymentMethod,
        vendor: body.vendor,
        status: body.status,
      };
      if (request.date) {
        request.date = DateUtil.parseFlexibleDate(request.date)?.toISOString() || request.date;
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
    @Query('from') from?: string,
    @Query('to') to?: string,
    @Query('category') category?: string,
  ): Promise<ExpenseResponse[]> {
    const fromDate = from ? DateUtil.parseFlexibleDate(from) : undefined;
    const toDate = to ? DateUtil.parseFlexibleDate(to) : undefined;
    return this.service.list(userId, fromDate, toDate, category);
  }

  @Get('me/:expenseId')
  async findByIdMe(
    @Param('expenseId') expenseId: string,
    @UserId() userId: string,
  ): Promise<ExpenseResponse> {
    return this.service.findById(expenseId, userId);
  }

  @Put('me/:expenseId')
  @UseInterceptors(FilesInterceptor('files'))
  async updateMe(
    @Param('expenseId') expenseId: string,
    @Body() body: any,
    @UserId() userId: string,
    @UploadedFiles() files?: Express.Multer.File[],
  ): Promise<ExpenseResponse> {
    // Se tem files, é multipart, senão é JSON
    if (files && files.length > 0) {
      try {
        const request: ExpenseRequest = {
          date: body.date,
          amount: body.amount ? parseFloat(body.amount) : undefined,
          description: body.description,
          category: body.category,
          paymentMethod: body.paymentMethod,
          vendor: body.vendor,
          status: body.status,
        };
        if (request.date) {
          request.date = DateUtil.parseFlexibleDate(request.date)?.toISOString() || request.date;
        }
        return this.service.update(expenseId, userId, request, files);
      } catch (e) {
        throw new HttpException(
          { error: e.message },
          HttpStatus.BAD_REQUEST,
        );
      }
    } else {
      // JSON request
      const request = body as ExpenseRequest;
      if (request.date && typeof request.date === 'string') {
        const parsed = DateUtil.parseFlexibleDate(request.date);
        if (parsed) {
          request.date = parsed.toISOString();
        }
      }
      return this.service.update(expenseId, userId, request);
    }
  }

  @Put('me/:expenseId/multipart')
  @UseInterceptors(FilesInterceptor('files'))
  async updateMeMultipart(
    @Param('expenseId') expenseId: string,
    @Body() body: any,
    @UserId() userId: string,
    @UploadedFiles() files?: Express.Multer.File[],
  ): Promise<ExpenseResponse> {
    try {
      const request: ExpenseRequest = {
        date: body.date,
        amount: body.amount ? parseFloat(body.amount) : undefined,
        description: body.description,
        category: body.category,
        paymentMethod: body.paymentMethod,
        vendor: body.vendor,
        status: body.status,
      };
      if (request.date) {
        request.date = DateUtil.parseFlexibleDate(request.date)?.toISOString() || request.date;
      }
      return this.service.update(expenseId, userId, request, files);
    } catch (e) {
      throw new HttpException(
        { error: e.message },
        HttpStatus.BAD_REQUEST,
      );
    }
  }

  @Delete('me/:expenseId')
  async deleteMe(
    @Param('expenseId') expenseId: string,
    @UserId() userId: string,
  ): Promise<{ success: boolean }> {
    try {
      await this.service.delete(expenseId, userId);
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
  ): Promise<ExpenseResponse> {
    // Se tem files, é multipart, senão é JSON
    if (files && files.length > 0) {
      try {
        const request: ExpenseRequest = {
          date: body.date,
          amount: body.amount ? parseFloat(body.amount) : undefined,
          description: body.description,
          category: body.category,
          paymentMethod: body.paymentMethod,
          vendor: body.vendor,
          status: body.status,
        };
        if (request.date) {
          request.date = DateUtil.parseFlexibleDate(request.date)?.toISOString() || request.date;
        }
        return this.service.create(userId, request, files);
      } catch (e) {
        throw new HttpException(
          { error: e.message },
          HttpStatus.BAD_REQUEST,
        );
      }
    } else {
      // JSON request
      const request = body as ExpenseRequest;
      if (request.date && typeof request.date === 'string') {
        const parsed = DateUtil.parseFlexibleDate(request.date);
        if (parsed) {
          request.date = parsed.toISOString();
        }
      }
      return this.service.create(userId, request);
    }
  }

  @Post('users/:userId/multipart')
  @UseInterceptors(FilesInterceptor('files'))
  async createForUserMultipart(
    @Param('userId') userId: string,
    @Body() body: any,
    @UploadedFiles() files?: Express.Multer.File[],
  ): Promise<ExpenseResponse> {
    try {
      const request: ExpenseRequest = {
        date: body.date,
        amount: body.amount ? parseFloat(body.amount) : undefined,
        description: body.description,
        category: body.category,
        paymentMethod: body.paymentMethod,
        vendor: body.vendor,
        status: body.status,
      };
      if (request.date) {
        request.date = DateUtil.parseFlexibleDate(request.date)?.toISOString() || request.date;
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
    @Query('from') from?: string,
    @Query('to') to?: string,
    @Query('category') category?: string,
  ): Promise<ExpenseResponse[]> {
    const fromDate = from ? DateUtil.parseFlexibleDate(from) : undefined;
    const toDate = to ? DateUtil.parseFlexibleDate(to) : undefined;
    return this.service.list(userId, fromDate, toDate, category);
  }
}
