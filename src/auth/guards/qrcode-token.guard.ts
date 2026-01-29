import {
  Injectable,
  CanActivate,
  ExecutionContext,
  UnauthorizedException,
} from '@nestjs/common';
import { QrCodeTokenService } from '../../qrcode-token/qrcode-token.service';

@Injectable()
export class QrCodeTokenGuard implements CanActivate {
  constructor(private qrCodeTokenService: QrCodeTokenService) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest();
    
    // Buscar token no query parameter ou no body
    const token = request.query?.token || request.body?.token;

    if (!token) {
      throw new UnauthorizedException('Token QR não fornecido');
    }

    const cleanToken = typeof token === 'string' ? token.trim() : String(token).trim();

    if (!cleanToken) {
      throw new UnauthorizedException('Token QR não fornecido ou formato inválido');
    }

    const isValid = await this.qrCodeTokenService.validateQrCodeToken(cleanToken);
    if (!isValid) {
      throw new UnauthorizedException('Token QR inválido, expirado ou inativo');
    }

    // Adicionar informações do usuário ao request
    request.userId = this.qrCodeTokenService.extractUserId(cleanToken);
    request.email = this.qrCodeTokenService.extractEmail(cleanToken);
    request.role = this.qrCodeTokenService.extractRole(cleanToken);
    request.qrToken = cleanToken; // Guardar o token para possível desativação após uso

    return true;
  }
}
