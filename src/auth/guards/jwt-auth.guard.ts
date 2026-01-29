import {
  Injectable,
  CanActivate,
  ExecutionContext,
  UnauthorizedException,
} from '@nestjs/common';
import { TokenService } from '../../token/token.service';

@Injectable()
export class JwtAuthGuard implements CanActivate {
  constructor(private tokenService: TokenService) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest();
    const authHeader = request.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      throw new UnauthorizedException('Token não fornecido ou formato inválido');
    }

    const token = authHeader.substring(7).trim();

    if (!token) {
      throw new UnauthorizedException('Token não fornecido ou formato inválido');
    }

    const isValid = await this.tokenService.validateToken(token);
    if (!isValid) {
      throw new UnauthorizedException('Token inválido, expirado ou inativo');
    }

    // Adicionar informações do usuário ao request
    request.userId = this.tokenService.extractUserId(token);
    request.email = this.tokenService.extractEmail(token);
    request.role = this.tokenService.extractRole(token);

    return true;
  }
}
