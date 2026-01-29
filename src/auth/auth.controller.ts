import {
  Controller,
  Post,
  Get,
  Body,
  Headers,
  HttpStatus,
  HttpException,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth } from '@nestjs/swagger';
import { AuthService } from './auth.service';
import { TokenService } from '../token/token.service';
import { LoginRequestDto } from './dto/login-request.dto';
import { LoginResponseDto } from './dto/login-response.dto';
import { JwtAuthGuard } from './guards/jwt-auth.guard';

@ApiTags('Autenticação')
@Controller('api/auth')
export class AuthController {
  constructor(
    private readonly authService: AuthService,
    private readonly tokenService: TokenService,
  ) {}

  @ApiOperation({ summary: 'Realizar login' })
  @ApiResponse({
    status: 200,
    description: 'Login realizado com sucesso',
    type: LoginResponseDto,
  })
  @ApiResponse({ status: 401, description: 'Credenciais inválidas' })
  @Post('login')
  async login(@Body() loginDto: LoginRequestDto): Promise<LoginResponseDto> {
    try {
      return await this.authService.login(loginDto);
    } catch (error) {
      // Se já é um HttpException, apenas relança
      if (error instanceof HttpException) {
        throw error;
      }
      // Caso contrário, cria um novo HttpException com a mensagem do erro
      throw new HttpException(
        { error: error.message || 'Erro ao realizar login' },
        HttpStatus.UNAUTHORIZED,
      );
    }
  }

  @ApiOperation({ summary: 'Realizar logout' })
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @ApiResponse({ status: 200, description: 'Logout realizado com sucesso' })
  @ApiResponse({ status: 401, description: 'Não autenticado' })
  @Post('logout')
  async logout(@Headers('authorization') authHeader: string) {
    try {
      if (!authHeader || !authHeader.startsWith('Bearer ')) {
        throw new HttpException(
          { error: 'Token não fornecido ou formato inválido' },
          HttpStatus.UNAUTHORIZED,
        );
      }

      const token = authHeader.substring(7);
      await this.authService.logout(token);
      return { message: 'Logout realizado com sucesso' };
    } catch (error) {
      throw new HttpException(
        { error: error.message || 'Erro ao realizar logout' },
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }

  @ApiOperation({ summary: 'Validar token' })
  @ApiResponse({
    status: 200,
    description: 'Validação realizada com sucesso',
    type: Boolean,
  })
  @Get('validate')
  async validateToken(
    @Headers('authorization') authHeader: string,
  ): Promise<boolean> {
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return false;
    }

    try {
      const token = authHeader.substring(7).trim();
      return await this.tokenService.validateToken(token);
    } catch (error) {
      return false;
    }
  }
}
