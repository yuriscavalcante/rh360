import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  OneToMany,
  JoinColumn,
  CreateDateColumn,
  UpdateDateColumn,
  DeleteDateColumn,
} from 'typeorm';
import { User } from './user.entity';
import { ExpenseAttachment } from './expense-attachment.entity';

@Entity('expenses')
export class Expense {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => User, { nullable: false })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column({ type: 'date' })
  date: Date;

  @Column({ type: 'decimal', precision: 19, scale: 2 })
  amount: number;

  @Column()
  description: string;

  @Column({ nullable: true })
  category: string;

  @Column({ name: 'payment_method', nullable: true })
  paymentMethod: string;

  @Column({ nullable: true })
  vendor: string;

  @Column({ nullable: true })
  status: string;

  @Column({ nullable: true })
  createdAt: string;

  @Column({ nullable: true })
  updatedAt: string;

  @Column({ nullable: true })
  deletedAt: string;

  @OneToMany(() => ExpenseAttachment, (attachment) => attachment.expense, {
    cascade: true,
  })
  attachments: ExpenseAttachment[];
}
