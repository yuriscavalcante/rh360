import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  OneToMany,
  CreateDateColumn,
  UpdateDateColumn,
  DeleteDateColumn,
} from 'typeorm';
import { Permission } from './permission.entity';
import { TimeClock } from './time-clock.entity';
import { TeamUser } from './team-user.entity';
import { Expense } from './expense.entity';
import { Salary } from './salary.entity';
import { Task } from './task.entity';

@Entity('users')
export class User {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ nullable: false })
  name: string;

  @Column({ unique: true, nullable: false })
  email: string;

  @Column({ nullable: false })
  password: string;

  @Column({ default: 'user' })
  role: string;

  @Column({ default: 'active' })
  status: string;

  @Column({ nullable: true })
  photo: string;

  @Column({ nullable: true })
  createdAt: string;

  @Column({ nullable: true })
  updatedAt: string;

  @Column({ nullable: true })
  deletedAt: string;

  @OneToMany(() => Permission, (permission) => permission.user, {
    cascade: true,
  })
  permissions: Permission[];

  @OneToMany(() => TimeClock, (timeClock) => timeClock.user)
  timeClocks: TimeClock[];

  @OneToMany(() => TeamUser, (teamUser) => teamUser.user)
  teamUsers: TeamUser[];

  @OneToMany(() => Expense, (expense) => expense.user)
  expenses: Expense[];

  @OneToMany(() => Salary, (salary) => salary.user)
  salaries: Salary[];

  @OneToMany(() => Task, (task) => task.responsibleUser)
  tasks: Task[];

}
