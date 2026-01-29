import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { JwtService } from '@nestjs/jwt';
import { ConfigService } from '@nestjs/config';
import { Token } from '../entities/token.entity';

@Injectable()
export class TokenService {
  constructor(
    @InjectRepository(Token)
    private tokenRepository: Repository<Token>,
    private jwtService: JwtService,
    private configService: ConfigService,
  ) {}

  async generateToken(
    userId: string,
    email: string,
    role: string,
  ): Promise<string> {
    const payload = {
      userId,
      email,
      role,
    };

    const expiration = this.configService.get<number>(
      'JWT_EXPIRATION',
      86400000,
    );

    return this.jwtService.signAsync(payload, {
      expiresIn: expiration / 1000, // Converter de ms para segundos
    });
  }

  async saveToken(tokenString: string, userId: string): Promise<Token> {
    const decoded = this.jwtService.decode(tokenString) as any;
    const expiresAt = new Date(decoded.exp * 1000);
    const createdAt = new Date();

    const token = this.tokenRepository.create({
      token: tokenString,
      userId,
      active: true,
      createdAt,
      expiresAt,
    });

    return this.tokenRepository.save(token);
  }

  extractEmail(token: string): string {
    const decoded = this.jwtService.decode(token) as any;
    return decoded.email;
  }

  extractUserId(token: string): string {
    const decoded = this.jwtService.decode(token) as any;
    return decoded.userId;
  }

  extractRole(token: string): string {
    const decoded = this.jwtService.decode(token) as any;
    return decoded.role;
  }

  async validateToken(token: string): Promise<boolean> {
    try {
      // Validar se o token JWT está válido
      await this.jwtService.verifyAsync(token);

      // Validar se o token está ativo no banco de dados
      const tokenEntity = await this.tokenRepository.findOne({
        where: { token, active: true },
      });

      return !!tokenEntity;
    } catch (error) {
      return false;
    }
  }

  async deactivateToken(token: string): Promise<void> {
    const tokenEntity = await this.tokenRepository.findOne({
      where: { token },
    });

    if (tokenEntity) {
      tokenEntity.active = false;
      await this.tokenRepository.save(tokenEntity);
    }
  }

  async deactivateAllUserTokens(userId: string): Promise<void> {
    const activeTokens = await this.tokenRepository.find({
      where: { userId, active: true },
    });

    for (const token of activeTokens) {
      token.active = false;
      await this.tokenRepository.save(token);
    }
  }

  generateQrCodeToken(
    userId: string,
    email: string,
    role: string,
  ): Promise<string> {
    const payload = {
      userId,
      email,
      role,
      type: 'qrcode',
    };

    const qrCodeExpiration = this.configService.get<number>(
      'JWT_QRCODE_EXPIRATION',
      900000,
    );

    return this.jwtService.signAsync(payload, {
      expiresIn: qrCodeExpiration / 1000,
    });
  }

  getQrCodeExpiration(): number {
    return this.configService.get<number>('JWT_QRCODE_EXPIRATION', 900000);
  }
}
