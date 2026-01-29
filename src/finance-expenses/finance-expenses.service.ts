import { Injectable, NotFoundException, ForbiddenException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Expense } from '../entities/expense.entity';
import { ExpenseAttachment } from '../entities/expense-attachment.entity';
import { User } from '../entities/user.entity';
import { ExpenseRequest } from './dto/expense-request.dto';
import { ExpenseResponse } from './dto/expense-response.dto';
import { DateUtil } from '../utils/date.util';

@Injectable()
export class FinanceExpensesService {
  constructor(
    @InjectRepository(Expense)
    private repository: Repository<Expense>,
    @InjectRepository(ExpenseAttachment)
    private attachmentRepository: Repository<ExpenseAttachment>,
    @InjectRepository(User)
    private usersRepository: Repository<User>,
  ) {}

  async create(
    userId: string,
    request: ExpenseRequest,
    files?: Express.Multer.File[],
  ): Promise<ExpenseResponse> {
    this.validateRequired(request);
    const user = await this.usersRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new NotFoundException('Usuário não encontrado');
    }

    const expense = new Expense();
    expense.user = user;
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
        expense: { id: saved.id },
        deletedAt: null,
      },
    });

    return this.toResponse(saved, attachments);
  }

  async update(
    expenseId: string,
    userId: string,
    request: ExpenseRequest,
    files?: Express.Multer.File[],
  ): Promise<ExpenseResponse> {
    const existing = await this.repository.findOne({
      where: { id: expenseId },
      relations: ['user'],
    });
    if (!existing || existing.deletedAt) {
      throw new NotFoundException('Despesa não encontrada');
    }
    if (!existing.user || existing.user.id !== userId) {
      throw new ForbiddenException('Você não tem permissão para atualizar esta despesa');
    }

    this.applyRequest(existing, request);
    existing.updatedAt = new Date().toISOString();
    const saved = await this.repository.save(existing);
    await this.saveAttachments(saved, files);
    const attachments = await this.attachmentRepository.find({
      where: {
        expense: { id: saved.id },
        deletedAt: null,
      },
    });

    return this.toResponse(saved, attachments);
  }

  async delete(expenseId: string, userId: string): Promise<void> {
    const existing = await this.repository.findOne({
      where: { id: expenseId },
      relations: ['user'],
    });
    if (!existing || existing.deletedAt) {
      throw new NotFoundException('Despesa não encontrada');
    }
    if (!existing.user || existing.user.id !== userId) {
      throw new ForbiddenException('Você não tem permissão para deletar esta despesa');
    }
    existing.deletedAt = new Date().toISOString();
    existing.updatedAt = new Date().toISOString();
    await this.repository.save(existing);
  }

  async findById(expenseId: string, userId: string): Promise<ExpenseResponse> {
    const existing = await this.repository.findOne({
      where: { id: expenseId },
      relations: ['user'],
    });
    if (!existing || existing.deletedAt) {
      throw new NotFoundException('Despesa não encontrada');
    }
    if (!existing.user || existing.user.id !== userId) {
      throw new ForbiddenException('Você não tem permissão para visualizar esta despesa');
    }
    const attachments = await this.attachmentRepository.find({
      where: {
        expense: { id: existing.id },
        deletedAt: null,
      },
    });
    return this.toResponse(existing, attachments);
  }

  async list(
    userId: string,
    from?: Date,
    to?: Date,
    category?: string,
  ): Promise<ExpenseResponse[]> {
    let query = this.repository
      .createQueryBuilder('expense')
      .leftJoinAndSelect('expense.user', 'user')
      .where('expense.deletedAt IS NULL')
      .andWhere('expense.user.id = :userId', { userId });

    if (from) {
      query = query.andWhere('expense.date >= :from', { from });
    }
    if (to) {
      query = query.andWhere('expense.date <= :to', { to });
    }
    if (category && category.trim()) {
      query = query.andWhere('expense.category = :category', { category: category.trim() });
    }

    query = query.orderBy('expense.date', 'DESC');
    const expenses = await query.getMany();

    const responses = await Promise.all(
      expenses.map(async (e) => {
        const attachments = await this.attachmentRepository.find({
          where: {
            expense: { id: e.id },
            deletedAt: null,
          },
        });
        return this.toResponse(e, attachments);
      }),
    );

    return responses;
  }

  async getExpensesForMonth(
    userId: string,
    from: Date,
    to: Date,
  ): Promise<number> {
    const expenses = await this.list(userId, from, to);
    return expenses.reduce((sum, e) => sum + (e.amount || 0), 0);
  }

  private validateRequired(request: ExpenseRequest): void {
    if (!request.date) {
      throw new Error('date é obrigatório');
    }
    if (request.amount === undefined || request.amount === null) {
      throw new Error('amount é obrigatório');
    }
    if (!request.description || request.description.trim() === '') {
      throw new Error('description é obrigatório');
    }
  }

  private applyRequest(expense: Expense, request: ExpenseRequest): void {
    if (request.date !== undefined) {
      expense.date = DateUtil.parseFlexibleDate(request.date);
    }
    if (request.amount !== undefined) {
      expense.amount = request.amount;
    }
    if (request.description !== undefined) {
      expense.description = request.description;
    }
    if (request.category !== undefined) {
      expense.category = request.category;
    }
    if (request.paymentMethod !== undefined) {
      expense.paymentMethod = request.paymentMethod;
    }
    if (request.vendor !== undefined) {
      expense.vendor = request.vendor;
    }
    if (request.status !== undefined) {
      expense.status = request.status;
    }
  }

  private async saveAttachments(
    expense: Expense,
    files?: Express.Multer.File[],
  ): Promise<void> {
    if (!files || files.length === 0) return;

    for (const file of files) {
      if (!file || file.size === 0) continue;
      // Placeholder URL - será substituído quando R2Storage for implementado
      const url = `https://placeholder.r2.dev/expenses/${expense.id}/${file.originalname}`;

      const attachment = new ExpenseAttachment();
      attachment.expense = expense;
      attachment.url = url;
      attachment.createdAt = new Date().toISOString();
      attachment.updatedAt = new Date().toISOString();
      await this.attachmentRepository.save(attachment);
    }
  }

  private toResponse(
    expense: Expense,
    attachments: ExpenseAttachment[],
  ): ExpenseResponse {
    return {
      id: expense.id,
      userId: expense.user?.id || '',
      date: expense.date,
      amount: expense.amount,
      description: expense.description,
      category: expense.category,
      paymentMethod: expense.paymentMethod,
      vendor: expense.vendor,
      status: expense.status,
      createdAt: expense.createdAt,
      updatedAt: expense.updatedAt,
      attachmentUrls: attachments.map((a) => a.url),
    };
  }
}
