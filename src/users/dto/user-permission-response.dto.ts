import { ApiProperty } from '@nestjs/swagger';

export class UserPermissionResponseDto {
  @ApiProperty({
    description: 'ID da permissão',
    example: '550e8400-e29b-41d4-a716-446655440000',
  })
  id: string;

  @ApiProperty({
    description: 'Função/permissão do sistema',
    example: 'CREATE_USER',
  })
  function: string;

  @ApiProperty({
    description: 'Indica se a permissão está permitida',
    example: true,
  })
  isPermitted: boolean;

  @ApiProperty({
    description: 'Data de criação da permissão',
    example: '2021-01-01',
  })
  createdAt: string;

  @ApiProperty({
    description: 'Data de atualização da permissão',
    example: '2021-01-01',
  })
  updatedAt: string;
}
