import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { User } from '../entities/user.entity';
import { TokenService } from '../token/token.service';
import { LoginRequestDto } from './dto/login-request.dto';
import { LoginResponseDto } from './dto/login-response.dto';

@Injectable()
export class AuthService {
  constructor(
    @InjectRepository(User)
    private usersRepository: Repository<User>,
    private tokenService: TokenService,
    private jwtService: JwtService,
  ) {}

  async login(loginDto: LoginRequestDto): Promise<LoginResponseDto> {
    const user = await this.usersRepository.findOne({
      where: { email: loginDto.email },
    });

    if (!user) {
      throw new UnauthorizedException('Usuário não encontrado');
    }

    if (user.status !== 'active') {
      throw new UnauthorizedException('Usuário inativo');
    }

    const isPasswordValid = await bcrypt.compare(
      loginDto.password,
      user.password,
    );

    if (!isPasswordValid) {
      throw new UnauthorizedException('Email ou Senha incorreta');
    }

    // Invalidar todos os tokens anteriores do usuário
    await this.tokenService.deactivateAllUserTokens(user.id);

    // Gerar novo token JWT
    const token = await this.tokenService.generateToken(
      user.id,
      user.email,
      user.role || 'user',
    );

    // Salvar novo token no banco de dados
    await this.tokenService.saveToken(token, user.id);

    return {
      id: user.id,
      token,
    };
  }

  async logout(token: string): Promise<void> {
    await this.tokenService.deactivateToken(token);
  }
}
