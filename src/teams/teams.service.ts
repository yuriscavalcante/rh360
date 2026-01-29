import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Team } from '../entities/team.entity';
import { TeamUser } from '../entities/team-user.entity';

@Injectable()
export class TeamsService {
  constructor(
    @InjectRepository(Team)
    private teamsRepository: Repository<Team>,
    @InjectRepository(TeamUser)
    private teamUsersRepository: Repository<TeamUser>,
  ) {}

  async findAll(): Promise<Team[]> {
    return this.teamsRepository.find({
      where: { status: 'active' },
      relations: ['teamUsers', 'teamUsers.user'],
    });
  }

  async findById(id: string): Promise<Team> {
    const team = await this.teamsRepository.findOne({
      where: { id },
      relations: ['teamUsers', 'teamUsers.user'],
    });

    if (!team) {
      throw new NotFoundException('Equipe não encontrada');
    }

    return team;
  }

  async create(teamData: Partial<Team>): Promise<Team> {
    const team = this.teamsRepository.create({
      ...teamData,
      status: 'active',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    });

    return this.teamsRepository.save(team);
  }

  async update(id: string, teamData: Partial<Team>): Promise<Team> {
    const team = await this.teamsRepository.findOne({ where: { id } });

    if (!team) {
      throw new NotFoundException('Equipe não encontrada');
    }

    Object.assign(team, teamData);
    team.updatedAt = new Date().toISOString();

    return this.teamsRepository.save(team);
  }

  async delete(id: string): Promise<void> {
    const team = await this.teamsRepository.findOne({ where: { id } });

    if (!team) {
      throw new NotFoundException('Equipe não encontrada');
    }

    team.status = 'deleted';
    team.updatedAt = new Date().toISOString();
    team.deletedAt = new Date().toISOString();

    await this.teamsRepository.save(team);
  }
}
