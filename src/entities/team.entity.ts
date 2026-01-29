import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  OneToMany,
} from 'typeorm';
import { TeamUser } from './team-user.entity';

@Entity('teams')
export class Team {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column()
  name: string;

  @Column({ nullable: true })
  description: string;

  @Column({ default: 'active' })
  status: string;

  @Column({ type: 'varchar', nullable: true })
  createdAt: string;

  @Column({ type: 'varchar', nullable: true })
  updatedAt: string;

  @Column({ type: 'varchar', nullable: true })
  deletedAt: string;

  @OneToMany(() => TeamUser, (teamUser) => teamUser.team, {
    cascade: true,
  })
  teamUsers: TeamUser[];
}
