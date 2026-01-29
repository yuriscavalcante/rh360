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
import { CompanyExpensesService } from './company-expenses.service';
import { CompanyExpenseRequest } from './dto/company-expense-request.dto';
import { CompanyExpenseResponse } from './dto/company-expense-response.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { DateUtil } from '../utils/date.util';

@Controller('api/finance/company-expenses')
@UseGuards(JwtAuthGuard)
export class CompanyExpensesController {
  constructor(private readonly service: CompanyExpensesService) {}

  @Post()
  @UseInterceptors(FilesInterceptor('files'))
  async create(
    @Body() body: any,
    @UploadedFiles() files?: Express.Multer.File[],
  ): Promise<CompanyExpenseResponse> {
    // Se tem files, é multipart, senão é JSON
    if (files && files.length > 0) {
      try {
        const request: CompanyExpenseRequest = {
          title: body.title,
          type: body.type,
          date: body.date,
          amount: body.amount ? parseFloat(body.amount) : undefined,
          description: body.description,
          status: body.status,
        };
        if (request.date) {
          request.date = DateUtil.parseFlexibleDate(request.date)?.toISOString() || request.date;
        }
        return this.service.create(request, files);
      } catch (e) {
        throw new HttpException(
          { error: e.message },
          HttpStatus.BAD_REQUEST,
        );
      }
    } else {
      // JSON request
      const request = body as CompanyExpenseRequest;
      if (request.date && typeof request.date === 'string') {
        const parsed = DateUtil.parseFlexibleDate(request.date);
        if (parsed) {
          request.date = parsed.toISOString();
        }
      }
      return this.service.create(request);
    }
  }

  @Post('multipart')
  @UseInterceptors(FilesInterceptor('files'))
  async createMultipart(
    @Body() body: any,
    @UploadedFiles() files?: Express.Multer.File[],
  ): Promise<CompanyExpenseResponse> {
    try {
      const request: CompanyExpenseRequest = {
        title: body.title,
        type: body.type,
        date: body.date,
        amount: body.amount ? parseFloat(body.amount) : undefined,
        description: body.description,
        status: body.status,
      };
      if (request.date) {
        request.date = DateUtil.parseFlexibleDate(request.date)?.toISOString() || request.date;
      }
      return this.service.create(request, files);
    } catch (e) {
      throw new HttpException(
        { error: e.message },
        HttpStatus.BAD_REQUEST,
      );
    }
  }

  @Get()
  async list(
    @Query('from') from?: string,
    @Query('to') to?: string,
    @Query('type') type?: string,
  ): Promise<CompanyExpenseResponse[]> {
    const fromDate = from ? DateUtil.parseFlexibleDate(from) : undefined;
    const toDate = to ? DateUtil.parseFlexibleDate(to) : undefined;
    return this.service.list(fromDate, toDate, type);
  }

  @Get(':id')
  async findById(@Param('id') id: string): Promise<CompanyExpenseResponse> {
    return this.service.findById(id);
  }

  @Put(':id')
  @UseInterceptors(FilesInterceptor('files'))
  async update(
    @Param('id') id: string,
    @Body() body: any,
    @UploadedFiles() files?: Express.Multer.File[],
  ): Promise<CompanyExpenseResponse> {
    // Se tem files, é multipart, senão é JSON
    if (files && files.length > 0) {
      try {
        const request: CompanyExpenseRequest = {
          title: body.title,
          type: body.type,
          date: body.date,
          amount: body.amount ? parseFloat(body.amount) : undefined,
          description: body.description,
          status: body.status,
        };
        if (request.date) {
          request.date = DateUtil.parseFlexibleDate(request.date)?.toISOString() || request.date;
        }
        return this.service.update(id, request, files);
      } catch (e) {
        throw new HttpException(
          { error: e.message },
          HttpStatus.BAD_REQUEST,
        );
      }
    } else {
      // JSON request
      const request = body as CompanyExpenseRequest;
      if (request.date && typeof request.date === 'string') {
        const parsed = DateUtil.parseFlexibleDate(request.date);
        if (parsed) {
          request.date = parsed.toISOString();
        }
      }
      return this.service.update(id, request);
    }
  }

  @Put(':id/multipart')
  @UseInterceptors(FilesInterceptor('files'))
  async updateMultipart(
    @Param('id') id: string,
    @Body() body: any,
    @UploadedFiles() files?: Express.Multer.File[],
  ): Promise<CompanyExpenseResponse> {
    try {
      const request: CompanyExpenseRequest = {
        title: body.title,
        type: body.type,
        date: body.date,
        amount: body.amount ? parseFloat(body.amount) : undefined,
        description: body.description,
        status: body.status,
      };
      if (request.date) {
        request.date = DateUtil.parseFlexibleDate(request.date)?.toISOString() || request.date;
      }
      return this.service.update(id, request, files);
    } catch (e) {
      throw new HttpException(
        { error: e.message },
        HttpStatus.BAD_REQUEST,
      );
    }
  }

  @Delete(':id')
  async delete(@Param('id') id: string): Promise<{ success: boolean }> {
    try {
      await this.service.delete(id);
      return { success: true };
    } catch (e) {
      throw new HttpException(
        { error: e.message },
        HttpStatus.BAD_REQUEST,
      );
    }
  }
}
