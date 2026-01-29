import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
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

  @Column()
  filename: string;

  @Column()
  url: string;

  @Column({ type: 'varchar', nullable: true })
  createdAt: string;
}
