import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
} from 'typeorm';
import { User } from './user.entity';

@Entity('permissions')
export class Permission {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ nullable: false })
  function: string;

  @Column({ name: 'is_permitted', nullable: false })
  isPermitted: boolean;

  @ManyToOne(() => User, (user) => user.permissions, { nullable: false })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column({ type: 'varchar', nullable: true })
  createdAt: string;

  @Column({ type: 'varchar', nullable: true })
  updatedAt: string;

  @Column({ type: 'varchar', nullable: true })
  deletedAt: string;
}
