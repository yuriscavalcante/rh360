import { ApiProperty } from '@nestjs/swagger';
import { IsBoolean, IsNotEmpty, IsString } from 'class-validator';

export class UserPermissionRequestDto {
  @ApiProperty({
    description: 'Função/permissão do sistema',
    example: 'CREATE_USER',
  })
  @IsString()
  @IsNotEmpty()
  function: string;

  @ApiProperty({
    description: 'Indica se a permissão está permitida',
    example: true,
  })
  @IsBoolean()
  @IsNotEmpty()
  isPermitted: boolean;
}
