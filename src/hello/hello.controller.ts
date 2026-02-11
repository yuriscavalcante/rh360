import { Controller, Get } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';

@ApiTags('Utilitários')
@Controller('api/hello')
export class HelloController {
  @ApiOperation({ summary: 'Endpoint de teste' })
  @ApiResponse({ status: 200, description: 'Mensagem de boas-vindas' })
  @Get()
  hello(): string {
    return 'Hello NestJS!';
  }
}
