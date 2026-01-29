import { ApiProperty } from '@nestjs/swagger';
import { UserPermissionResponseDto } from './user-permission-response.dto';

export class UserResponseDto {
  @ApiProperty({
    description: 'ID do usuário',
    example: '550e8400-e29b-41d4-a716-446655440000',
  })
  id: string;

  @ApiProperty({ description: 'Nome do usuário', example: 'João da Silva' })
  name: string;

  @ApiProperty({ description: 'Email do usuário', example: 'joao.silva@example.com' })
  email: string;

  @ApiProperty({ description: 'Role do usuário', example: 'admin' })
  role: string;

  @ApiProperty({ description: 'Status do usuário', example: 'active' })
  status: string;

  @ApiProperty({
    description: 'Data de criação do usuário',
    example: '2021-01-01',
  })
  createdAt: string;

  @ApiProperty({
    description: 'Data de atualização do usuário',
    example: '2021-01-01',
  })
  updatedAt: string;

  @ApiProperty({
    description: 'URL da foto do usuário',
    example: 'https://pub-xxx.r2.dev/users/photo.jpg',
  })
  photo: string;

  @ApiProperty({
    description: 'Permissões do usuário',
    type: [UserPermissionResponseDto],
    required: false,
  })
  permissions?: UserPermissionResponseDto[];
}
