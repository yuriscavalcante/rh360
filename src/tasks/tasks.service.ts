import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, Like, IsNull } from 'typeorm';
import { Task } from '../entities/task.entity';
import { User } from '../entities/user.entity';
import { Team } from '../entities/team.entity';
import { TaskRequest } from './dto/task-request.dto';
import { TaskResponse } from './dto/task-response.dto';
import { UserResponseDto } from '../users/dto/user-response.dto';

@Injectable()
export class TasksService {
  constructor(
    @InjectRepository(Task)
    private taskRepository: Repository<Task>,
    @InjectRepository(User)
    private usersRepository: Repository<User>,
    @InjectRepository(Team)
    private teamRepository: Repository<Team>,
  ) {}

  async create(request: TaskRequest): Promise<TaskResponse> {
    const task = new Task();
    task.title = request.title;
    task.description = request.description;
    task.status = request.status || 'pending';
    task.startDate = request.startDate;
    task.endDate = request.endDate;
    task.createdAt = new Date().toISOString();
    task.updatedAt = new Date().toISOString();

    if (request.responsibleUserId) {
      const user = await this.usersRepository.findOne({
        where: { id: request.responsibleUserId },
      });
      if (!user) {
        throw new NotFoundException('Usuário não encontrado');
      }
      task.responsibleUser = user;
    }

    if (request.teamId) {
      const team = await this.teamRepository.findOne({
        where: { id: request.teamId },
      });
      if (!team) {
        throw new NotFoundException('Equipe não encontrada');
      }
      task.team = team;
    }

    if (request.parentTaskId) {
      const parentTask = await this.taskRepository.findOne({
        where: { id: request.parentTaskId },
      });
      if (!parentTask) {
        throw new NotFoundException('Tarefa pai não encontrada');
      }
      task.parentTask = parentTask;
    }

    const saved = await this.taskRepository.save(task);
    return this.toResponse(saved, true, true);
  }

  async findAll(
    page: number = 0,
    size: number = 20,
    sort?: string,
    search?: string,
  ): Promise<{ content: TaskResponse[]; totalElements: number; totalPages: number; size: number; number: number }> {
    const skip = page * size;
    let query = this.taskRepository.createQueryBuilder('task')
      .leftJoinAndSelect('task.responsibleUser', 'responsibleUser')
      .leftJoinAndSelect('task.team', 'team')
      .where('task.deletedAt IS NULL');

    if (search && search.trim()) {
      query = query.andWhere('LOWER(task.title) LIKE LOWER(:search)', {
        search: `%${search.trim()}%`,
      });
    }

    if (sort) {
      const [field, direction] = sort.split(',');
      query = query.orderBy(`task.${field}`, direction.toUpperCase() as 'ASC' | 'DESC');
    } else {
      query = query.orderBy('task.createdAt', 'DESC');
    }

    const [tasks, total] = await query.skip(skip).take(size).getManyAndCount();

    return {
      content: tasks.map((t) => this.toResponse(t, false, false)),
      totalElements: total,
      totalPages: Math.ceil(total / size),
      size,
      number: page,
    };
  }

  async findRootTasks(
    page: number = 0,
    size: number = 20,
    sort?: string,
  ): Promise<{ content: TaskResponse[]; totalElements: number; totalPages: number; size: number; number: number }> {
    const skip = page * size;
    let query = this.taskRepository.createQueryBuilder('task')
      .leftJoinAndSelect('task.responsibleUser', 'responsibleUser')
      .leftJoinAndSelect('task.team', 'team')
      .where('task.deletedAt IS NULL')
      .andWhere('task.parentTask IS NULL');

    if (sort) {
      const [field, direction] = sort.split(',');
      query = query.orderBy(`task.${field}`, direction.toUpperCase() as 'ASC' | 'DESC');
    } else {
      query = query.orderBy('task.createdAt', 'DESC');
    }

    const [tasks, total] = await query.skip(skip).take(size).getManyAndCount();

    return {
      content: tasks.map((t) => this.toResponse(t, false, false)),
      totalElements: total,
      totalPages: Math.ceil(total / size),
      size,
      number: page,
    };
  }

  async findByUserId(
    userId: string,
    page: number = 0,
    size: number = 20,
    sort?: string,
  ): Promise<{ content: TaskResponse[]; totalElements: number; totalPages: number; size: number; number: number }> {
    const skip = page * size;
    let query = this.taskRepository.createQueryBuilder('task')
      .leftJoinAndSelect('task.responsibleUser', 'responsibleUser')
      .leftJoinAndSelect('task.team', 'team')
      .where('task.deletedAt IS NULL')
      .andWhere('task.responsibleUser.id = :userId', { userId });

    if (sort) {
      const [field, direction] = sort.split(',');
      query = query.orderBy(`task.${field}`, direction.toUpperCase() as 'ASC' | 'DESC');
    } else {
      query = query.orderBy('task.createdAt', 'DESC');
    }

    const [tasks, total] = await query.skip(skip).take(size).getManyAndCount();

    return {
      content: tasks.map((t) => this.toResponse(t, false, false)),
      totalElements: total,
      totalPages: Math.ceil(total / size),
      size,
      number: page,
    };
  }

  async findByTeamId(
    teamId: string,
    page: number = 0,
    size: number = 20,
    sort?: string,
  ): Promise<{ content: TaskResponse[]; totalElements: number; totalPages: number; size: number; number: number }> {
    const skip = page * size;
    let query = this.taskRepository.createQueryBuilder('task')
      .leftJoinAndSelect('task.responsibleUser', 'responsibleUser')
      .leftJoinAndSelect('task.team', 'team')
      .where('task.deletedAt IS NULL')
      .andWhere('task.team.id = :teamId', { teamId });

    if (sort) {
      const [field, direction] = sort.split(',');
      query = query.orderBy(`task.${field}`, direction.toUpperCase() as 'ASC' | 'DESC');
    } else {
      query = query.orderBy('task.createdAt', 'DESC');
    }

    const [tasks, total] = await query.skip(skip).take(size).getManyAndCount();

    return {
      content: tasks.map((t) => this.toResponse(t, false, false)),
      totalElements: total,
      totalPages: Math.ceil(total / size),
      size,
      number: page,
    };
  }

  async findSubtasks(
    parentTaskId: string,
    page: number = 0,
    size: number = 20,
    sort?: string,
  ): Promise<{ content: TaskResponse[]; totalElements: number; totalPages: number; size: number; number: number }> {
    const parentTask = await this.taskRepository.findOne({
      where: { id: parentTaskId },
    });
    if (!parentTask) {
      throw new NotFoundException('Tarefa pai não encontrada');
    }

    const skip = page * size;
    let query = this.taskRepository.createQueryBuilder('task')
      .leftJoinAndSelect('task.responsibleUser', 'responsibleUser')
      .leftJoinAndSelect('task.team', 'team')
      .where('task.deletedAt IS NULL')
      .andWhere('task.parentTask.id = :parentTaskId', { parentTaskId });

    if (sort) {
      const [field, direction] = sort.split(',');
      query = query.orderBy(`task.${field}`, direction.toUpperCase() as 'ASC' | 'DESC');
    } else {
      query = query.orderBy('task.createdAt', 'DESC');
    }

    const [tasks, total] = await query.skip(skip).take(size).getManyAndCount();

    return {
      content: tasks.map((t) => this.toResponse(t, false, false)),
      totalElements: total,
      totalPages: Math.ceil(total / size),
      size,
      number: page,
    };
  }

  async findById(id: string): Promise<TaskResponse> {
    const task = await this.taskRepository.findOne({
      where: { id },
      relations: ['responsibleUser', 'team', 'parentTask', 'subtasks'],
    });

    if (!task) {
      throw new NotFoundException('Tarefa não encontrada');
    }

    return this.toResponse(task, true, true);
  }

  async update(id: string, request: TaskRequest): Promise<TaskResponse> {
    const task = await this.taskRepository.findOne({
      where: { id },
      relations: ['responsibleUser', 'team', 'parentTask'],
    });

    if (!task) {
      throw new NotFoundException('Tarefa não encontrada');
    }

    if (request.title !== undefined) {
      task.title = request.title;
    }
    if (request.description !== undefined) {
      task.description = request.description;
    }
    if (request.startDate !== undefined) {
      task.startDate = request.startDate;
    }
    if (request.endDate !== undefined) {
      task.endDate = request.endDate;
    }
    if (request.status !== undefined) {
      task.status = request.status;
    }

    if (request.responsibleUserId !== undefined) {
      if (request.responsibleUserId) {
        const user = await this.usersRepository.findOne({
          where: { id: request.responsibleUserId },
        });
        if (!user) {
          throw new NotFoundException('Usuário não encontrado');
        }
        task.responsibleUser = user;
      } else {
        task.responsibleUser = null;
      }
    }

    if (request.teamId !== undefined) {
      if (request.teamId) {
        const team = await this.teamRepository.findOne({
          where: { id: request.teamId },
        });
        if (!team) {
          throw new NotFoundException('Equipe não encontrada');
        }
        task.team = team;
      } else {
        task.team = null;
      }
    }

    if (request.parentTaskId !== undefined) {
      if (request.parentTaskId) {
        if (request.parentTaskId === id) {
          throw new Error('Uma tarefa não pode ser pai de si mesma');
        }
        const parentTask = await this.taskRepository.findOne({
          where: { id: request.parentTaskId },
        });
        if (!parentTask) {
          throw new NotFoundException('Tarefa pai não encontrada');
        }
        task.parentTask = parentTask;
      } else {
        task.parentTask = null;
      }
    }

    task.updatedAt = new Date().toISOString();
    const saved = await this.taskRepository.save(task);
    return this.toResponse(saved, true, true);
  }

  async delete(id: string): Promise<void> {
    const task = await this.taskRepository.findOne({ where: { id } });
    if (!task) {
      throw new NotFoundException('Tarefa não encontrada');
    }

    task.status = 'deleted';
    task.updatedAt = new Date().toISOString();
    task.deletedAt = new Date().toISOString();
    await this.taskRepository.save(task);
  }

  private toResponse(
    task: Task,
    includeSubtasks = false,
    includeParent = false,
  ): TaskResponse {
    const response: TaskResponse = {
      id: task.id,
      title: task.title,
      description: task.description,
      status: task.status,
      startDate: task.startDate,
      endDate: task.endDate,
      createdAt: task.createdAt,
      updatedAt: task.updatedAt,
    };

    if (task.responsibleUser) {
      response.responsibleUser = {
        id: task.responsibleUser.id,
        name: task.responsibleUser.name,
        email: task.responsibleUser.email,
        role: task.responsibleUser.role,
        status: task.responsibleUser.status,
        photo: task.responsibleUser.photo,
        createdAt: task.responsibleUser.createdAt,
        updatedAt: task.responsibleUser.updatedAt,
      } as UserResponseDto;
    }

    if (task.team) {
      response.team = {
        id: task.team.id,
        name: task.team.name,
        description: task.team.description,
        status: task.team.status,
        createdAt: task.team.createdAt,
        updatedAt: task.team.updatedAt,
      };
    }

    if (includeParent && task.parentTask) {
      response.parentTask = this.toResponse(task.parentTask, false, false);
    }

    if (includeSubtasks && task.subtasks && task.subtasks.length > 0) {
      response.subtasks = task.subtasks.map((st) =>
        this.toResponse(st, false, false),
      );
    } else {
      response.subtasks = [];
    }

    return response;
  }
}
