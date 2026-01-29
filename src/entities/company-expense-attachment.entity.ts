import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
  CreateDateColumn,
} from 'typeorm';
import { CompanyExpense } from './company-expense.entity';

@Entity('company_expense_attachments')
export class CompanyExpenseAttachment {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(
    () => CompanyExpense,
    (companyExpense) => companyExpense.attachments,
    { onDelete: 'CASCADE' },
  )
  @JoinColumn({ name: 'company_expense_id' })
  companyExpense: CompanyExpense;

  @Column({ nullable: false, length: 2048 })
  url: string;

  @Column({ name: 'created_at', nullable: true })
  createdAt: string;

  @Column({ name: 'updated_at', nullable: true })
  updatedAt: string;

  @Column({ name: 'deleted_at', nullable: true })
  deletedAt: string;
}
