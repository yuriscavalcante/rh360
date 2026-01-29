import { ApiProperty } from '@nestjs/swagger';
import { IsEmail, IsNotEmpty, IsOptional, IsString } from 'class-validator';
import { UserPermissionRequestDto } from './user-permission-request.dto';

export class UserRequestDto {
  @ApiProperty({ description: 'Nome completo do usuário', example: 'João Silva' })
  @IsString()
  @IsNotEmpty()
  name: string;

  @ApiProperty({
    description: 'Email do usuário (deve ser único)',
    example: 'joao.silva@example.com',
  })
  @IsEmail()
  @IsNotEmpty()
  email: string;

  @ApiProperty({ description: 'Senha do usuário', example: 'senha123' })
  @IsString()
  @IsOptional()
  password?: string;

  @ApiProperty({ description: 'Papel/função do usuário no sistema', example: 'user' })
  @IsString()
  @IsOptional()
  role?: string;

  @ApiProperty({ description: 'Status do usuário', example: 'active' })
  @IsString()
  @IsOptional()
  status?: string;

  @ApiProperty({ description: 'Lista de permissões do usuário', required: false })
  @IsOptional()
  permissions?: UserPermissionRequestDto[];
}
