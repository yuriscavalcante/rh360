import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Team } from '../entities/team.entity';
import { TeamUser } from '../entities/team-user.entity';
import { User } from '../entities/user.entity';
import { UserResponseDto } from '../users/dto/user-response.dto';
import { TeamsGateway } from './teams.gateway';

@Injectable()
export class TeamsService {
  constructor(
    @InjectRepository(Team)
    private teamsRepository: Repository<Team>,
    @InjectRepository(TeamUser)
    private teamUsersRepository: Repository<TeamUser>,
    @InjectRepository(User)
    private usersRepository: Repository<User>,
    private teamsGateway: TeamsGateway,
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

    const saved = await this.teamsRepository.save(team);
    this.teamsGateway.emitTeamCreated(saved);
    return saved;
  }

  async update(id: string, teamData: Partial<Team>): Promise<Team> {
    const team = await this.teamsRepository.findOne({ where: { id } });

    if (!team) {
      throw new NotFoundException('Equipe não encontrada');
    }

    Object.assign(team, teamData);
    team.updatedAt = new Date().toISOString();

    const saved = await this.teamsRepository.save(team);
    this.teamsGateway.emitTeamUpdated(saved);
    return saved;
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
    this.teamsGateway.emitTeamDeleted(id);
  }

  async findUsersByTeamId(teamId: string): Promise<UserResponseDto[]> {
    const team = await this.teamsRepository.findOne({ where: { id: teamId } });
    if (!team) {
      throw new NotFoundException('Equipe não encontrada');
    }
    const teamUsers = await this.teamUsersRepository.find({
      where: { teamId },
      relations: ['user', 'user.permissions'],
    });
    const users = teamUsers.map((teamUser) => teamUser.user);
    return users.map((user) => this.toUserResponseDto(user));
  }

  async addUserToTeam(teamId: string, userId: string): Promise<void> {
    const team = await this.teamsRepository.findOne({ where: { id: teamId } });
    if (!team) throw new NotFoundException('Equipe não encontrada');
    const user = await this.usersRepository.findOne({ where: { id: userId } });
    if (!user) throw new NotFoundException('Usuário não encontrado');
    const existing = await this.teamUsersRepository.findOne({ where: { teamId, userId } });
    if (existing) return;
    await this.teamUsersRepository.save(
      this.teamUsersRepository.create({ teamId, userId }),
    );
    this.teamsGateway.emitTeamsListUpdated();
    this.teamsGateway.emitTeamUsersUpdated(teamId);
  }

  async removeUserFromTeam(teamId: string, userId: string): Promise<void> {
    const result = await this.teamUsersRepository.delete({ teamId, userId });
    if (result.affected && result.affected > 0) {
      this.teamsGateway.emitTeamsListUpdated();
      this.teamsGateway.emitTeamUsersUpdated(teamId);
    }
  }

  private toUserResponseDto(user: User): UserResponseDto {
    return {
      id: user.id,
      name: user.name,
      email: user.email,
      role: user.role,
      status: user.status,
      createdAt: user.createdAt,
      updatedAt: user.updatedAt,
      photo: user.photo || '',
      permissions: user.permissions
        ? user.permissions.map((perm) => ({
            id: perm.id,
            function: perm.function,
            isPermitted: perm.isPermitted,
            createdAt: perm.createdAt,
            updatedAt: perm.updatedAt,
          }))
        : undefined,
    };
  }
}
