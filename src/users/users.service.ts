import {
  Injectable,
  NotFoundException,
  ConflictException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, Like } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { User } from '../entities/user.entity';
import { Permission } from '../entities/permission.entity';
import { UserRequestDto } from './dto/user-request.dto';
import { UserResponseDto } from './dto/user-response.dto';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private usersRepository: Repository<User>,
    @InjectRepository(Permission)
    private permissionRepository: Repository<Permission>,
  ) {}

  async create(
    userDto: UserRequestDto,
    photo?: Express.Multer.File,
  ): Promise<UserResponseDto> {
    // Verificar se o email já existe
    const existingUser = await this.usersRepository.findOne({
      where: { email: userDto.email },
    });

    if (existingUser) {
      throw new ConflictException('Email já cadastrado');
    }

    const user = this.usersRepository.create({
      name: userDto.name,
      email: userDto.email,
      password: userDto.password
        ? await bcrypt.hash(userDto.password, 10)
        : undefined,
      role: userDto.role || 'user',
      status: userDto.status || 'active',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    });

    const savedUser = await this.usersRepository.save(user);

    // Salvar permissões se fornecidas
    if (userDto.permissions && userDto.permissions.length > 0) {
      const permissions = userDto.permissions.map((perm) =>
        this.permissionRepository.create({
          function: perm.function,
          isPermitted: perm.isPermitted,
          user: savedUser,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        }),
      );
      await this.permissionRepository.save(permissions);
    }

    return this.toResponseDto(savedUser);
  }

  async findAll(
    page: number = 0,
    size: number = 20,
    search?: string,
  ): Promise<{ data: UserResponseDto[]; total: number }> {
    const skip = page * size;
    const where = search ? { name: Like(`%${search}%`) } : {};

    const [users, total] = await this.usersRepository.findAndCount({
      where,
      skip,
      take: size,
      order: { createdAt: 'DESC' },
    });

    return {
      data: users.map((user) => this.toResponseDto(user)),
      total,
    };
  }

  async findById(id: string): Promise<UserResponseDto> {
    const user = await this.usersRepository.findOne({
      where: { id },
      relations: ['permissions'],
    });

    if (!user) {
      throw new NotFoundException('Usuário não encontrado');
    }

    return this.toResponseDto(user);
  }

  async update(
    id: string,
    userDto: UserRequestDto,
    photo?: Express.Multer.File,
  ): Promise<UserResponseDto> {
    const user = await this.usersRepository.findOne({ where: { id } });

    if (!user) {
      throw new NotFoundException('Usuário não encontrado');
    }

    // Verificar se o email já está sendo usado por outro usuário
    if (userDto.email && userDto.email !== user.email) {
      const existingUser = await this.usersRepository.findOne({
        where: { email: userDto.email },
      });

      if (existingUser) {
        throw new ConflictException('Email já cadastrado');
      }
    }

    user.name = userDto.name || user.name;
    user.email = userDto.email || user.email;
    user.role = userDto.role || user.role;
    user.status = userDto.status || user.status;
    user.updatedAt = new Date().toISOString();

    if (userDto.password) {
      user.password = await bcrypt.hash(userDto.password, 10);
    }

    const updatedUser = await this.usersRepository.save(user);

    // Atualizar permissões se fornecidas
    if (userDto.permissions) {
      // Deletar permissões antigas
      await this.permissionRepository.delete({ user: { id } });

      // Criar novas permissões
      if (userDto.permissions.length > 0) {
        const permissions = userDto.permissions.map((perm) =>
          this.permissionRepository.create({
            function: perm.function,
            isPermitted: perm.isPermitted,
            user: updatedUser,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          }),
        );
        await this.permissionRepository.save(permissions);
      }
    }

    return this.toResponseDto(updatedUser);
  }

  async delete(id: string): Promise<void> {
    const user = await this.usersRepository.findOne({ where: { id } });

    if (!user) {
      throw new NotFoundException('Usuário não encontrado');
    }

    user.status = 'deleted';
    user.updatedAt = new Date().toISOString();
    user.deletedAt = new Date().toISOString();

    await this.usersRepository.save(user);
  }

  private toResponseDto(user: User): UserResponseDto {
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
