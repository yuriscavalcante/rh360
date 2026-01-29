import {
  Entity,
  ManyToOne,
  JoinColumn,
  PrimaryColumn,
} from 'typeorm';
import { Team } from './team.entity';
import { User } from './user.entity';

@Entity('team_users')
export class TeamUser {
  @PrimaryColumn({ type: 'uuid', name: 'team_id' })
  teamId: string;

  @PrimaryColumn({ type: 'uuid', name: 'user_id' })
  userId: string;

  @ManyToOne(() => Team, (team) => team.teamUsers, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'team_id' })
  team: Team;

  @ManyToOne(() => User, (user) => user.teamUsers, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;
}
