import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
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

  @Column()
  filename: string;

  @Column()
  url: string;

  @Column({ type: 'varchar', nullable: true })
  createdAt: string;
}
