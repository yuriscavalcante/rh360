import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { TeamsService } from './teams.service';
import { Team } from '../entities/team.entity';
import { UserResponseDto } from '../users/dto/user-response.dto';

@ApiTags('Equipes')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('api/teams')
export class TeamsController {
  constructor(private readonly teamsService: TeamsService) {}

  @ApiOperation({ summary: 'Listar todas as equipes' })
  @Get()
  async findAll(): Promise<Team[]> {
    return this.teamsService.findAll();
  }

  @ApiOperation({ summary: 'Listar usuários de uma equipe' })
  @ApiResponse({
    status: 200,
    description: 'Lista de usuários da equipe retornada com sucesso',
    type: [UserResponseDto],
  })
  @Get(':id/users')
  async findUsersByTeamId(@Param('id') id: string): Promise<UserResponseDto[]> {
    return this.teamsService.findUsersByTeamId(id);
  }

  @ApiOperation({ summary: 'Buscar equipe por ID' })
  @Get(':id')
  async findById(@Param('id') id: string): Promise<Team> {
    return this.teamsService.findById(id);
  }

  @ApiOperation({ summary: 'Criar nova equipe' })
  @Post()
  async create(@Body() teamData: Partial<Team>): Promise<Team> {
    return this.teamsService.create(teamData);
  }

  @ApiOperation({ summary: 'Atualizar equipe' })
  @Put(':id')
  async update(
    @Param('id') id: string,
    @Body() teamData: Partial<Team>,
  ): Promise<Team> {
    return this.teamsService.update(id, teamData);
  }

  @ApiOperation({ summary: 'Deletar equipe' })
  @Delete(':id')
  async delete(@Param('id') id: string): Promise<void> {
    return this.teamsService.delete(id);
  }
}
