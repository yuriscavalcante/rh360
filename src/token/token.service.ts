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
      expiresIn: `${expiration / 1000}s`, // Converter de ms para segundos e passar como string
    });
  }

  async saveToken(tokenString: string, userId: string): Promise<Token> {
    // Garantir que o token seja salvo sem espaços
    const cleanToken = tokenString.trim();
    
    const decoded = this.jwtService.decode(cleanToken) as any;
    if (!decoded || !decoded.exp) {
      throw new Error('Token inválido: não foi possível decodificar');
    }
    
    const expiresAt = new Date(decoded.exp * 1000);
    const createdAt = new Date();

    // Verificar se o token já existe no banco
    const existingToken = await this.tokenRepository.findOne({
      where: { token: cleanToken },
    });

    if (existingToken) {
      // Se o token já existe, atualizar para ativo (garantir que está ativo)
      existingToken.active = true;
      existingToken.userId = userId;
      existingToken.expiresAt = expiresAt;
      existingToken.createdAt = createdAt;
      return this.tokenRepository.save(existingToken);
    }

    // Se não existe, criar novo token como ativo
    const token = this.tokenRepository.create({
      token: cleanToken,
      userId,
      active: true,
      createdAt,
      expiresAt,
    });

    return this.tokenRepository.save(token);
  }

  extractEmail(token: string): string {
    const cleanToken = token.trim();
    const decoded = this.jwtService.decode(cleanToken) as any;
    return decoded?.email;
  }

  extractUserId(token: string): string {
    const cleanToken = token.trim();
    const decoded = this.jwtService.decode(cleanToken) as any;
    return decoded?.userId;
  }

  extractRole(token: string): string {
    const cleanToken = token.trim();
    const decoded = this.jwtService.decode(cleanToken) as any;
    return decoded?.role;
  }

  async validateToken(token: string): Promise<boolean> {
    try {
      // Limpar o token de possíveis espaços
      const cleanToken = token.trim();
      
      if (!cleanToken) {
        return false;
      }

      // Primeiro validar se o token JWT está válido (não expirado e assinatura correta)
      let decoded: any;
      try {
        decoded = await this.jwtService.verifyAsync(cleanToken);
      } catch (jwtError) {
        // Token JWT inválido ou expirado
        return false;
      }

      // Extrair userId do token decodificado
      const userId = decoded.userId;
      if (!userId) {
        return false;
      }

      // Buscar token no banco de dados pelo token (que é único)
      const tokenEntity = await this.tokenRepository.findOne({
        where: { 
          token: cleanToken,
          active: true 
        },
      });

      if (!tokenEntity) {
        return false;
      }

      // Verificar se o userId do token corresponde ao userId no banco
      if (tokenEntity.userId !== userId) {
        return false;
      }

      // Verificar se o token não expirou no banco de dados
      const now = new Date();
      if (tokenEntity.expiresAt < now) {
        return false;
      }

      return true;
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
    await this.tokenRepository
      .createQueryBuilder()
      .update(Token)
      .set({ active: false })
      .where('user_id = :userId', { userId })
      .andWhere('active = :active', { active: true })
      .execute();
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
      expiresIn: `${qrCodeExpiration / 1000}s`, // Converter de ms para segundos e passar como string
    });
  }

  getQrCodeExpiration(): number {
    return this.configService.get<number>('JWT_QRCODE_EXPIRATION', 900000);
  }
}
