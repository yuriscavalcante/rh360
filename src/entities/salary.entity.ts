import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  OneToMany,
  JoinColumn,
} from 'typeorm';
import { User } from './user.entity';
import { SalaryAttachment } from './salary-attachment.entity';

@Entity('salaries')
export class Salary {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => User, { nullable: false })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column({ name: 'reference_month' })
  referenceMonth: string;

  @Column({ name: 'gross_amount', type: 'decimal', precision: 19, scale: 2 })
  grossAmount: number;

  @Column({
    name: 'net_amount',
    type: 'decimal',
    precision: 19,
    scale: 2,
    nullable: true,
  })
  netAmount: number;

  @Column({
    name: 'discounts',
    type: 'decimal',
    precision: 19,
    scale: 2,
    nullable: true,
  })
  discounts: number;

  @Column({
    name: 'bonuses',
    type: 'decimal',
    precision: 19,
    scale: 2,
    nullable: true,
  })
  bonuses: number;

  @Column({ name: 'paid_at', type: 'date', nullable: true })
  paidAt: Date;

  @Column({ type: 'text', nullable: true })
  notes: string;

  @Column({ nullable: true })
  status: string;

  @Column({ type: 'varchar', nullable: true })
  createdAt: string;

  @Column({ type: 'varchar', nullable: true })
  updatedAt: string;

  @Column({ type: 'varchar', nullable: true })
  deletedAt: string;

  @OneToMany(() => SalaryAttachment, (attachment) => attachment.salary, {
    cascade: true,
  })
  attachments: SalaryAttachment[];
}
