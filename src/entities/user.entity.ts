import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  OneToMany,
  CreateDateColumn,
  UpdateDateColumn,
} from 'typeorm';
import { Permission } from './permission.entity';
import { TimeClock } from './time-clock.entity';
import { TeamUser } from './team-user.entity';

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

  @Column({ type: 'varchar', nullable: true })
  createdAt: string;

  @Column({ type: 'varchar', nullable: true })
  updatedAt: string;

  @Column({ type: 'varchar', nullable: true })
  deletedAt: string;

  @OneToMany(() => Permission, (permission) => permission.user, {
    cascade: true,
  })
  permissions: Permission[];

  @OneToMany(() => TimeClock, (timeClock) => timeClock.user)
  timeClocks: TimeClock[];

  @OneToMany(() => TeamUser, (teamUser) => teamUser.user)
  teamUsers: TeamUser[];
}
