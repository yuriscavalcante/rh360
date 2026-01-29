import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  OneToMany,
  ManyToOne,
  JoinColumn,
  CreateDateColumn,
  UpdateDateColumn,
  DeleteDateColumn,
} from 'typeorm';
import { CompanyExpenseAttachment } from './company-expense-attachment.entity';

@Entity('company_expenses')
export class CompanyExpense {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ nullable: false })
  title: string;

  @Column()
  type: string;

  @Column({ type: 'date' })
  date: Date;

  @Column({ type: 'decimal', precision: 19, scale: 2, nullable: true })
  amount: number;

  @Column({ type: 'text', nullable: true })
  description: string;

  @Column({ nullable: true })
  status: string;

  @Column({ name: 'created_at', nullable: true })
  createdAt: string;

  @Column({ name: 'updated_at', nullable: true })
  updatedAt: string;

  @Column({ name: 'deleted_at', nullable: true })
  deletedAt: string;

  @OneToMany(
    () => CompanyExpenseAttachment,
    (attachment) => attachment.companyExpense,
    { cascade: true },
  )
  attachments: CompanyExpenseAttachment[];
}
