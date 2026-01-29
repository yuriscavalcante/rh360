import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
} from 'typeorm';
import { Expense } from './expense.entity';

@Entity('expense_attachments')
export class ExpenseAttachment {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => Expense, (expense) => expense.attachments, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'expense_id' })
  expense: Expense;

  @Column()
  filename: string;

  @Column()
  url: string;

  @Column({ type: 'varchar', nullable: true })
  createdAt: string;
}
