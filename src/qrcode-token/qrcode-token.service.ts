import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { JwtService } from '@nestjs/jwt';
import { ConfigService } from '@nestjs/config';
import { QrCodeToken } from '../entities/qrcode-token.entity';

@Injectable()
export class QrCodeTokenService {
  constructor(
    @InjectRepository(QrCodeToken)
    private qrCodeTokenRepository: Repository<QrCodeToken>,
    private jwtService: JwtService,
    private configService: ConfigService,
  ) {}

  /**
   * Gera um token JWT específico para QR code
   */
  async generateQrCodeToken(
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

  /**
   * Salva um token QR no banco de dados
   */
  async saveQrCodeToken(
    tokenString: string,
    userId: string,
  ): Promise<QrCodeToken> {
    // Garantir que o token seja salvo sem espaços
    const cleanToken = tokenString.trim();

    const decoded = this.jwtService.decode(cleanToken) as any;
    if (!decoded || !decoded.exp) {
      throw new Error('Token inválido: não foi possível decodificar');
    }

    const expiresAt = new Date(decoded.exp * 1000);
    const createdAt = new Date();

    // Verificar se o token já existe no banco
    const existingToken = await this.qrCodeTokenRepository.findOne({
      where: { token: cleanToken },
    });

    if (existingToken) {
      // Se o token já existe, atualizar para ativo (garantir que está ativo)
      existingToken.active = true;
      existingToken.userId = userId;
      existingToken.expiresAt = expiresAt;
      existingToken.createdAt = createdAt;
      return this.qrCodeTokenRepository.save(existingToken);
    }

    // Se não existe, criar novo token como ativo
    const token = this.qrCodeTokenRepository.create({
      token: cleanToken,
      userId,
      active: true,
      createdAt,
      expiresAt,
    });

    return this.qrCodeTokenRepository.save(token);
  }

  /**
   * Valida um token QR
   */
  async validateQrCodeToken(token: string): Promise<boolean> {
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

      // Verificar se é um token do tipo qrcode
      if (decoded.type !== 'qrcode') {
        return false;
      }

      // Extrair userId do token decodificado
      const userId = decoded.userId;
      if (!userId) {
        return false;
      }

      // Buscar token no banco de dados pelo token (que é único)
      const tokenEntity = await this.qrCodeTokenRepository.findOne({
        where: {
          token: cleanToken,
          active: true,
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

  /**
   * Extrai o userId de um token QR
   */
  extractUserId(token: string): string {
    const cleanToken = token.trim();
    const decoded = this.jwtService.decode(cleanToken) as any;
    return decoded?.userId;
  }

  /**
   * Extrai o email de um token QR
   */
  extractEmail(token: string): string {
    const cleanToken = token.trim();
    const decoded = this.jwtService.decode(cleanToken) as any;
    return decoded?.email;
  }

  /**
   * Extrai o role de um token QR
   */
  extractRole(token: string): string {
    const cleanToken = token.trim();
    const decoded = this.jwtService.decode(cleanToken) as any;
    return decoded?.role;
  }

  /**
   * Desativa um token QR
   */
  async deactivateQrCodeToken(token: string): Promise<void> {
    const tokenEntity = await this.qrCodeTokenRepository.findOne({
      where: { token },
    });

    if (tokenEntity) {
      tokenEntity.active = false;
      await this.qrCodeTokenRepository.save(tokenEntity);
    }
  }

  /**
   * Desativa todos os tokens QR de um usuário
   */
  async deactivateAllUserQrCodeTokens(userId: string): Promise<void> {
    await this.qrCodeTokenRepository
      .createQueryBuilder()
      .update(QrCodeToken)
      .set({ active: false })
      .where('user_id = :userId', { userId })
      .andWhere('active = :active', { active: true })
      .execute();
  }

  /**
   * Retorna o tempo de expiração do token QR em milissegundos
   */
  getQrCodeExpiration(): number {
    return this.configService.get<number>('JWT_QRCODE_EXPIRATION', 900000);
  }
}
