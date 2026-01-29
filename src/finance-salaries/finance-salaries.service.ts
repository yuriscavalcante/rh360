import { Injectable, NotFoundException, ForbiddenException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Salary } from '../entities/salary.entity';
import { SalaryAttachment } from '../entities/salary-attachment.entity';
import { User } from '../entities/user.entity';
import { SalaryRequest } from './dto/salary-request.dto';
import { SalaryResponse } from './dto/salary-response.dto';
import { DateUtil } from '../utils/date.util';

@Injectable()
export class FinanceSalariesService {
  constructor(
    @InjectRepository(Salary)
    private repository: Repository<Salary>,
    @InjectRepository(SalaryAttachment)
    private attachmentRepository: Repository<SalaryAttachment>,
    @InjectRepository(User)
    private usersRepository: Repository<User>,
  ) {}

  async create(
    userId: string,
    request: SalaryRequest,
    files?: Express.Multer.File[],
  ): Promise<SalaryResponse> {
    this.validateRequired(request);
    const user = await this.usersRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new NotFoundException('Usuário não encontrado');
    }

    const salary = new Salary();
    salary.user = user;
    this.applyRequest(salary, request);
    salary.createdAt = new Date().toISOString();
    salary.updatedAt = new Date().toISOString();
    if (!salary.status || salary.status.trim() === '') {
      salary.status = 'pending';
    }

    const saved = await this.repository.save(salary);
    await this.saveAttachments(saved, files);
    const attachments = await this.attachmentRepository.find({
      where: {
        salary: { id: saved.id },
        deletedAt: null,
      },
    });

    return this.toResponse(saved, attachments);
  }

  async update(
    salaryId: string,
    userId: string,
    request: SalaryRequest,
    files?: Express.Multer.File[],
  ): Promise<SalaryResponse> {
    const existing = await this.repository.findOne({
      where: { id: salaryId },
      relations: ['user'],
    });
    if (!existing || existing.deletedAt) {
      throw new NotFoundException('Salário não encontrado');
    }
    if (!existing.user || existing.user.id !== userId) {
      throw new ForbiddenException('Você não tem permissão para atualizar este salário');
    }

    this.applyRequest(existing, request);
    existing.updatedAt = new Date().toISOString();
    const saved = await this.repository.save(existing);
    await this.saveAttachments(saved, files);
    const attachments = await this.attachmentRepository.find({
      where: {
        salary: { id: saved.id },
        deletedAt: null,
      },
    });

    return this.toResponse(saved, attachments);
  }

  async delete(salaryId: string, userId: string): Promise<void> {
    const existing = await this.repository.findOne({
      where: { id: salaryId },
      relations: ['user'],
    });
    if (!existing || existing.deletedAt) {
      throw new NotFoundException('Salário não encontrado');
    }
    if (!existing.user || existing.user.id !== userId) {
      throw new ForbiddenException('Você não tem permissão para deletar este salário');
    }
    existing.deletedAt = new Date().toISOString();
    existing.updatedAt = new Date().toISOString();
    await this.repository.save(existing);
  }

  async findById(salaryId: string, userId: string): Promise<SalaryResponse> {
    const existing = await this.repository.findOne({
      where: { id: salaryId },
      relations: ['user'],
    });
    if (!existing || existing.deletedAt) {
      throw new NotFoundException('Salário não encontrado');
    }
    if (!existing.user || existing.user.id !== userId) {
      throw new ForbiddenException('Você não tem permissão para visualizar este salário');
    }
    const attachments = await this.attachmentRepository.find({
      where: {
        salary: { id: existing.id },
        deletedAt: null,
      },
    });
    return this.toResponse(existing, attachments);
  }

  async list(
    userId: string,
    referenceMonth?: string,
    fromPaidAt?: Date,
    toPaidAt?: Date,
  ): Promise<SalaryResponse[]> {
    let query = this.repository
      .createQueryBuilder('salary')
      .leftJoinAndSelect('salary.user', 'user')
      .where('salary.deletedAt IS NULL')
      .andWhere('salary.user.id = :userId', { userId });

    if (referenceMonth && referenceMonth.trim()) {
      query = query.andWhere('salary.referenceMonth = :referenceMonth', {
        referenceMonth: referenceMonth.trim(),
      });
    }
    if (fromPaidAt) {
      query = query.andWhere('salary.paidAt >= :fromPaidAt', { fromPaidAt });
    }
    if (toPaidAt) {
      query = query.andWhere('salary.paidAt <= :toPaidAt', { toPaidAt });
    }

    query = query.orderBy('salary.referenceMonth', 'DESC');
    const salaries = await query.getMany();

    const responses = await Promise.all(
      salaries.map(async (s) => {
        const attachments = await this.attachmentRepository.find({
          where: {
            salary: { id: s.id },
            deletedAt: null,
          },
        });
        return this.toResponse(s, attachments);
      }),
    );

    return responses;
  }

  async getIncomeForMonth(userId: string, referenceMonth: string): Promise<number> {
    const salaries = await this.list(userId, referenceMonth);
    return salaries.reduce((sum, s) => {
      const value = s.netAmount || s.grossAmount || 0;
      return sum + value;
    }, 0);
  }

  private validateRequired(request: SalaryRequest): void {
    if (!request.referenceMonth || request.referenceMonth.trim() === '') {
      throw new Error('referenceMonth é obrigatório');
    }
    if (request.grossAmount === undefined || request.grossAmount === null) {
      throw new Error('grossAmount é obrigatório');
    }
  }

  private applyRequest(salary: Salary, request: SalaryRequest): void {
    if (request.referenceMonth !== undefined) {
      salary.referenceMonth = request.referenceMonth;
    }
    if (request.grossAmount !== undefined) {
      salary.grossAmount = request.grossAmount;
    }
    if (request.netAmount !== undefined) {
      salary.netAmount = request.netAmount;
    }
    if (request.discounts !== undefined) {
      salary.discounts = request.discounts;
    }
    if (request.bonuses !== undefined) {
      salary.bonuses = request.bonuses;
    }
    if (request.paidAt !== undefined) {
      salary.paidAt = DateUtil.parseFlexibleDate(request.paidAt);
    }
    if (request.notes !== undefined) {
      salary.notes = request.notes;
    }
    if (request.status !== undefined) {
      salary.status = request.status;
    }
  }

  private async saveAttachments(
    salary: Salary,
    files?: Express.Multer.File[],
  ): Promise<void> {
    if (!files || files.length === 0) return;

    for (const file of files) {
      if (!file || file.size === 0) continue;
      // Placeholder URL - será substituído quando R2Storage for implementado
      const url = `https://placeholder.r2.dev/salaries/${salary.id}/${file.originalname}`;

      const attachment = new SalaryAttachment();
      attachment.salary = salary;
      attachment.url = url;
      attachment.createdAt = new Date().toISOString();
      attachment.updatedAt = new Date().toISOString();
      await this.attachmentRepository.save(attachment);
    }
  }

  private toResponse(
    salary: Salary,
    attachments: SalaryAttachment[],
  ): SalaryResponse {
    return {
      id: salary.id,
      userId: salary.user?.id || '',
      referenceMonth: salary.referenceMonth,
      grossAmount: salary.grossAmount,
      netAmount: salary.netAmount,
      discounts: salary.discounts,
      bonuses: salary.bonuses,
      paidAt: salary.paidAt,
      notes: salary.notes,
      status: salary.status,
      createdAt: salary.createdAt,
      updatedAt: salary.updatedAt,
      attachmentUrls: attachments.map((a) => a.url),
    };
  }
}
