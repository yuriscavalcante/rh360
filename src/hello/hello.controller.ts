import { Controller, Get } from '@nestjs/common';

@Controller('api/hello')
export class HelloController {
  @Get()
  hello(): string {
    return 'Hello NestJS!';
  }
}
