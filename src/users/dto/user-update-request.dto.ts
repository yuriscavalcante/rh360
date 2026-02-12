import { ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsEmail,
  IsOptional,
  IsString,
  IsArray,
  ValidateNested,
} from 'class-validator';
import { Type } from 'class-transformer';
import { Transform } from 'class-transformer';
import { UserPermissionRequestDto } from './user-permission-request.dto';

const emptyStringToUndefined = ({ value }: { value: unknown }) =>
  value === '' || value === null ? undefined : value;

/**
 * DTO para atualização de usuário. Todos os campos são opcionais.
 * Valores vazios ou nulos são ignorados (não alteram o campo).
 * Em FormData, campos não enviados ou enviados vazios não alteram o valor atual.
 */
export class UserUpdateRequestDto {
  @ApiPropertyOptional({ description: 'Nome completo do usuário' })
  @IsOptional()
  @IsString()
  @Transform(emptyStringToUndefined)
  name?: string;

  @ApiPropertyOptional({ description: 'Email do usuário (deve ser único)' })
  @IsOptional()
  @IsEmail()
  @Transform(emptyStringToUndefined)
  email?: string;

  @ApiPropertyOptional({ description: 'Nova senha (só altera se informada)' })
  @IsOptional()
  @IsString()
  @Transform(emptyStringToUndefined)
  password?: string;

  @ApiPropertyOptional({ description: 'Papel/função do usuário no sistema' })
  @IsOptional()
  @IsString()
  @Transform(emptyStringToUndefined)
  role?: string;

  @ApiPropertyOptional({ description: 'Status do usuário' })
  @IsOptional()
  @IsString()
  @Transform(emptyStringToUndefined)
  status?: string;

  @ApiPropertyOptional({ description: 'Lista de permissões (JSON no FormData)' })
  @IsOptional()
  @Transform(({ value }) => {
    if (value === '' || value === null || value === undefined) return undefined;
    if (Array.isArray(value)) return value;
    if (typeof value === 'string') {
      try {
        const parsed = JSON.parse(value);
        return Array.isArray(parsed) ? parsed : undefined;
      } catch {
        return undefined;
      }
    }
    return undefined;
  })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => UserPermissionRequestDto)
  permissions?: UserPermissionRequestDto[];
}
