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

  @Column({ name: 'created_at', nullable: true })
  createdAt: string;

  @Column({ name: 'updated_at', nullable: true })
  updatedAt: string;

  @Column({ name: 'deleted_at', nullable: true })
  deletedAt: string;
}
