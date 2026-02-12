import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { TeamsController } from './teams.controller';
import { TeamsService } from './teams.service';
import { TeamsGateway } from './teams.gateway';
import { Team } from '../entities/team.entity';
import { TeamUser } from '../entities/team-user.entity';
import { User } from '../entities/user.entity';
import { TokenModule } from '../token/token.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([Team, TeamUser, User]),
    TokenModule,
  ],
  controllers: [TeamsController],
  providers: [TeamsService, TeamsGateway],
  exports: [TeamsService],
})
export class TeamsModule {}
