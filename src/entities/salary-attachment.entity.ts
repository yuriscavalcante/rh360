import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
  CreateDateColumn,
} from 'typeorm';
import { Salary } from './salary.entity';

@Entity('salary_attachments')
export class SalaryAttachment {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => Salary, (salary) => salary.attachments, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'salary_id' })
  salary: Salary;

  @Column({ nullable: false, length: 2048 })
  url: string;

  @Column({ nullable: true })
  createdAt: string;

  @Column({ nullable: true })
  updatedAt: string;

  @Column({ nullable: true })
  deletedAt: string;
}
