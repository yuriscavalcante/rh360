import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CompanyExpense } from '../entities/company-expense.entity';
import { CompanyExpenseAttachment } from '../entities/company-expense-attachment.entity';
import { CompanyExpenseRequest } from './dto/company-expense-request.dto';
import { CompanyExpenseResponse } from './dto/company-expense-response.dto';
import { DateUtil } from '../utils/date.util';

@Injectable()
export class CompanyExpensesService {
  constructor(
    @InjectRepository(CompanyExpense)
    private repository: Repository<CompanyExpense>,
    @InjectRepository(CompanyExpenseAttachment)
    private attachmentRepository: Repository<CompanyExpenseAttachment>,
  ) {}

  async create(
    request: CompanyExpenseRequest,
    files?: Express.Multer.File[],
  ): Promise<CompanyExpenseResponse> {
    this.validateRequired(request);

    const expense = new CompanyExpense();
    this.applyRequest(expense, request);
    expense.createdAt = new Date().toISOString();
    expense.updatedAt = new Date().toISOString();
    if (!expense.status || expense.status.trim() === '') {
      expense.status = 'paid';
    }

    const saved = await this.repository.save(expense);
    await this.saveAttachments(saved, files);
    const attachments = await this.attachmentRepository.find({
      where: {
        companyExpense: { id: saved.id },
        deletedAt: null,
      },
    });

    return this.toResponse(saved, attachments);
  }

  async update(
    id: string,
    request: CompanyExpenseRequest,
    files?: Express.Multer.File[],
  ): Promise<CompanyExpenseResponse> {
    const existing = await this.repository.findOne({ where: { id } });
    if (!existing || existing.deletedAt) {
      throw new NotFoundException('Gasto não encontrado');
    }

    this.applyRequest(existing, request);
    existing.updatedAt = new Date().toISOString();
    const saved = await this.repository.save(existing);
    await this.saveAttachments(saved, files);
    const attachments = await this.attachmentRepository.find({
      where: {
        companyExpense: { id: saved.id },
        deletedAt: null,
      },
    });

    return this.toResponse(saved, attachments);
  }

  async delete(id: string): Promise<void> {
    const existing = await this.repository.findOne({ where: { id } });
    if (!existing || existing.deletedAt) {
      throw new NotFoundException('Gasto não encontrado');
    }
    existing.deletedAt = new Date().toISOString();
    existing.updatedAt = new Date().toISOString();
    await this.repository.save(existing);
  }

  async findById(id: string): Promise<CompanyExpenseResponse> {
    const existing = await this.repository.findOne({ where: { id } });
    if (!existing || existing.deletedAt) {
      throw new NotFoundException('Gasto não encontrado');
    }
    const attachments = await this.attachmentRepository.find({
      where: {
        companyExpense: { id: existing.id },
        deletedAt: null,
      },
    });
    return this.toResponse(existing, attachments);
  }

  async list(
    from?: Date,
    to?: Date,
    type?: string,
  ): Promise<CompanyExpenseResponse[]> {
    let query = this.repository
      .createQueryBuilder('expense')
      .where('expense.deletedAt IS NULL');

    if (from) {
      query = query.andWhere('expense.date >= :from', { from });
    }
    if (to) {
      query = query.andWhere('expense.date <= :to', { to });
    }
    if (type && type.trim()) {
      query = query.andWhere('expense.type = :type', { type: type.trim() });
    }

    query = query.orderBy('expense.date', 'DESC');
    const expenses = await query.getMany();

    const responses = await Promise.all(
      expenses.map(async (e) => {
        const attachments = await this.attachmentRepository.find({
          where: {
            companyExpense: { id: e.id },
            deletedAt: null,
          },
        });
        return this.toResponse(e, attachments);
      }),
    );

    return responses;
  }

  private validateRequired(request: CompanyExpenseRequest): void {
    if (!request.title || request.title.trim() === '') {
      throw new Error('title é obrigatório');
    }
    if (!request.type || request.type.trim() === '') {
      throw new Error('type é obrigatório');
    }
    if (!request.date) {
      throw new Error('date é obrigatório');
    }
  }

  private applyRequest(
    expense: CompanyExpense,
    request: CompanyExpenseRequest,
  ): void {
    if (request.title !== undefined) {
      expense.title = request.title;
    }
    if (request.type !== undefined) {
      expense.type = request.type;
    }
    if (request.date !== undefined) {
      expense.date = DateUtil.parseFlexibleDate(request.date);
    }
    if (request.amount !== undefined) {
      expense.amount = request.amount;
    }
    if (request.description !== undefined) {
      expense.description = request.description;
    }
    if (request.status !== undefined) {
      expense.status = request.status;
    }
  }

  private async saveAttachments(
    expense: CompanyExpense,
    files?: Express.Multer.File[],
  ): Promise<void> {
    if (!files || files.length === 0) return;

    // TODO: Implementar upload para R2Storage
    // Por enquanto, apenas criar os registros de attachment
    for (const file of files) {
      if (!file || file.size === 0) continue;
      // Placeholder URL - será substituído quando R2Storage for implementado
      const url = `https://placeholder.r2.dev/company-expenses/${expense.id}/${file.originalname}`;

      const attachment = new CompanyExpenseAttachment();
      attachment.companyExpense = expense;
      attachment.url = url;
      attachment.createdAt = new Date().toISOString();
      attachment.updatedAt = new Date().toISOString();
      await this.attachmentRepository.save(attachment);
    }
  }

  private toResponse(
    expense: CompanyExpense,
    attachments: CompanyExpenseAttachment[],
  ): CompanyExpenseResponse {
    return {
      id: expense.id,
      title: expense.title,
      type: expense.type,
      date: expense.date,
      amount: expense.amount,
      description: expense.description,
      status: expense.status,
      createdAt: expense.createdAt,
      updatedAt: expense.updatedAt,
      attachmentUrls: attachments.map((a) => a.url),
    };
  }
}
