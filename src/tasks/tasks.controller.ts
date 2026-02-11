import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  Query,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse, ApiQuery, ApiParam } from '@nestjs/swagger';
import { TasksService } from './tasks.service';
import { TaskRequest } from './dto/task-request.dto';
import { TaskResponse } from './dto/task-response.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';

@ApiTags('Tarefas')
@Controller('api/tasks')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class TasksController {
  constructor(private readonly service: TasksService) {}

  @ApiOperation({ summary: 'Criar tarefa' })
  @ApiResponse({ status: 201, description: 'Tarefa criada', type: TaskResponse })
  @Post()
  async create(@Body() request: TaskRequest): Promise<TaskResponse> {
    return this.service.create(request);
  }

  @ApiOperation({ summary: 'Listar tarefas com paginação' })
  @ApiQuery({ name: 'page', required: false })
  @ApiQuery({ name: 'size', required: false })
  @ApiQuery({ name: 'sort', required: false })
  @ApiQuery({ name: 'search', required: false })
  @ApiResponse({ status: 200, description: 'Lista de tarefas' })
  @Get()
  async findAll(
    @Query('page') page?: string,
    @Query('size') size?: string,
    @Query('sort') sort?: string,
    @Query('search') search?: string,
  ) {
    return this.service.findAll(
      page ? parseInt(page, 10) : 0,
      size ? parseInt(size, 10) : 20,
      sort,
      search,
    );
  }

  @ApiOperation({ summary: 'Listar tarefas raiz' })
  @Get('root')
  async findRootTasks(
    @Query('page') page?: string,
    @Query('size') size?: string,
    @Query('sort') sort?: string,
  ) {
    return this.service.findRootTasks(
      page ? parseInt(page, 10) : 0,
      size ? parseInt(size, 10) : 20,
      sort,
    );
  }

  @ApiOperation({ summary: 'Listar tarefas por usuário' })
  @ApiParam({ name: 'userId' })
  @Get('users/:userId')
  async findByUserId(
    @Param('userId') userId: string,
    @Query('page') page?: string,
    @Query('size') size?: string,
    @Query('sort') sort?: string,
  ) {
    return this.service.findByUserId(
      userId,
      page ? parseInt(page, 10) : 0,
      size ? parseInt(size, 10) : 20,
      sort,
    );
  }

  @ApiOperation({ summary: 'Listar tarefas por equipe' })
  @ApiParam({ name: 'teamId' })
  @Get('teams/:teamId')
  async findByTeamId(
    @Param('teamId') teamId: string,
    @Query('page') page?: string,
    @Query('size') size?: string,
    @Query('sort') sort?: string,
  ) {
    return this.service.findByTeamId(
      teamId,
      page ? parseInt(page, 10) : 0,
      size ? parseInt(size, 10) : 20,
      sort,
    );
  }

  @ApiOperation({ summary: 'Listar subtarefas' })
  @ApiParam({ name: 'parentTaskId' })
  @Get(':parentTaskId/subtasks')
  async findSubtasks(
    @Param('parentTaskId') parentTaskId: string,
    @Query('page') page?: string,
    @Query('size') size?: string,
    @Query('sort') sort?: string,
  ) {
    return this.service.findSubtasks(
      parentTaskId,
      page ? parseInt(page, 10) : 0,
      size ? parseInt(size, 10) : 20,
      sort,
    );
  }

  @ApiOperation({ summary: 'Buscar tarefa por ID' })
  @ApiParam({ name: 'id' })
  @ApiResponse({ status: 200, type: TaskResponse })
  @Get(':id')
  async findById(@Param('id') id: string): Promise<TaskResponse> {
    return this.service.findById(id);
  }

  @ApiOperation({ summary: 'Atualizar tarefa' })
  @ApiParam({ name: 'id' })
  @ApiResponse({ status: 200, type: TaskResponse })
  @Put(':id')
  async update(
    @Param('id') id: string,
    @Body() request: TaskRequest,
  ): Promise<TaskResponse> {
    return this.service.update(id, request);
  }

  @ApiOperation({ summary: 'Excluir tarefa' })
  @ApiParam({ name: 'id' })
  @ApiResponse({ status: 200 })
  @Delete(':id')
  async delete(@Param('id') id: string): Promise<void> {
    return this.service.delete(id);
  }
}
