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
import { TasksService } from './tasks.service';
import { TaskRequest } from './dto/task-request.dto';
import { TaskResponse } from './dto/task-response.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';

@Controller('api/tasks')
@UseGuards(JwtAuthGuard)
export class TasksController {
  constructor(private readonly service: TasksService) {}

  @Post()
  async create(@Body() request: TaskRequest): Promise<TaskResponse> {
    return this.service.create(request);
  }

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

  @Get(':id')
  async findById(@Param('id') id: string): Promise<TaskResponse> {
    return this.service.findById(id);
  }

  @Put(':id')
  async update(
    @Param('id') id: string,
    @Body() request: TaskRequest,
  ): Promise<TaskResponse> {
    return this.service.update(id, request);
  }

  @Delete(':id')
  async delete(@Param('id') id: string): Promise<void> {
    return this.service.delete(id);
  }
}
