import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  OneToMany,
} from 'typeorm';
import { CompanyExpenseAttachment } from './company-expense-attachment.entity';

@Entity('company_expenses')
export class CompanyExpense {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column()
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

  @Column({ type: 'varchar', nullable: true })
  createdAt: string;

  @Column({ type: 'varchar', nullable: true })
  updatedAt: string;

  @Column({ type: 'varchar', nullable: true })
  deletedAt: string;

  @OneToMany(
    () => CompanyExpenseAttachment,
    (attachment) => attachment.companyExpense,
    { cascade: true },
  )
  attachments: CompanyExpenseAttachment[];
}
