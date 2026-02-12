import {
  Injectable,
  NotFoundException,
  ConflictException,
  Inject,
  forwardRef,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, Like } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { User } from '../entities/user.entity';
import { Permission } from '../entities/permission.entity';
import { UserRequestDto } from './dto/user-request.dto';
import { UserUpdateRequestDto } from './dto/user-update-request.dto';
import { UserResponseDto } from './dto/user-response.dto';
import { UsersGateway } from './users.gateway';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private usersRepository: Repository<User>,
    @InjectRepository(Permission)
    private permissionRepository: Repository<Permission>,
    @Inject(forwardRef(() => UsersGateway))
    private usersGateway: UsersGateway,
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

    const userResponse = this.toResponseDto(savedUser);
    
    // Emitir evento WebSocket para notificar sobre o novo usuário
    this.usersGateway.emitUserCreated(userResponse);

    return userResponse;
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
    userDto: UserUpdateRequestDto,
    photo?: Express.Multer.File,
  ): Promise<UserResponseDto> {
    const user = await this.usersRepository.findOne({ where: { id } });

    if (!user) {
      throw new NotFoundException('Usuário não encontrado');
    }

    // Só altera campos que foram enviados (não undefined, não null, não string vazia)
    const hasValue = (v: unknown): v is string =>
      v != null && typeof v === 'string' && v.trim() !== '';

    if (hasValue(userDto.name)) user.name = userDto.name.trim();
    if (hasValue(userDto.email)) {
      const existingUser = await this.usersRepository.findOne({
        where: { email: userDto.email.trim() },
      });
      if (existingUser && existingUser.id !== id) {
        throw new ConflictException('Email já cadastrado');
      }
      user.email = userDto.email.trim();
    }
    if (hasValue(userDto.role)) user.role = userDto.role.trim();
    if (hasValue(userDto.status)) user.status = userDto.status.trim();
    if (hasValue(userDto.password)) {
      user.password = await bcrypt.hash(userDto.password, 10);
    }

    user.updatedAt = new Date().toISOString();

    const updatedUser = await this.usersRepository.save(user);

    // Atualizar permissões apenas se o array foi enviado (pode ser [] para limpar)
    if (userDto.permissions !== undefined && Array.isArray(userDto.permissions)) {
      await this.permissionRepository.delete({ user: { id } });
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

    // Recarrega com relations para a resposta incluir permissões
    const userResponse = await this.findById(id);

    // Emitir para a lista geral (WebSocket): clientes na listagem recebem user:updated
    this.usersGateway.emitUserUpdated(userResponse);

    return userResponse;
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
    
    // Emitir evento WebSocket para notificar sobre a remoção do usuário
    this.usersGateway.emitUserDeleted(id);
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
