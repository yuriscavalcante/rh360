import {
  WebSocketGateway,
  WebSocketServer,
  OnGatewayConnection,
  OnGatewayDisconnect,
} from '@nestjs/websockets';
import { Logger } from '@nestjs/common';
import { Server, Socket } from 'socket.io';
import { TokenService } from '../token/token.service';

@WebSocketGateway({
  cors: {
    origin: '*',
  },
  namespace: '/users',
})
export class UsersGateway implements OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer()
  server: Server;

  private logger: Logger = new Logger('UsersGateway');

  constructor(private readonly tokenService: TokenService) {}

  handleConnection(client: Socket) {
    this.logger.log(`[WebSocket] Cliente conectado ao namespace /users: ${client.id}`);
    client.join('users-list');

    const token = client.handshake.auth?.token as string | undefined;
    if (token) {
      const userId = this.tokenService.extractUserId(token);
      if (userId) {
        client.join(`user:${userId}`);
        this.logger.log(`[WebSocket] Cliente ${client.id} entrou na sala user:${userId} (users/me)`);
      }
    }

    const total = client.nsp.sockets.size;
    this.logger.log(`[WebSocket] Cliente entrou na sala "users-list". Total de clientes conectados: ${total}`);
  }

  handleDisconnect(client: Socket) {
    const restantes = client.nsp.sockets.size;
    this.logger.log(`[WebSocket] Cliente desconectado do namespace /users: ${client.id}. Restam ${restantes} cliente(s) conectado(s).`);
  }

  /** Emite para a lista geral de usuários (sala users-list). */
  emitUserCreated(user: any) {
    this.server.to('users-list').emit('user:created', user);
    this.logger.log(`Evento user:created emitido para lista geral (${user.id})`);
  }

  /** Emite para a lista geral de usuários (sala users-list). */
  emitUserUpdated(user: any) {
    this.server.to('users-list').emit('user:updated', user);
    this.logger.log(`Evento user:updated emitido para lista geral (${user.id})`);
  }

  /** Emite para a lista geral de usuários (sala users-list). */
  emitUserDeleted(userId: string) {
    this.server.to('users-list').emit('user:deleted', { id: userId });
    this.logger.log(`Evento user:deleted emitido para lista geral (${userId})`);
  }

  // Método para emitir atualização completa da lista
  emitUsersListUpdated(users: any) {
    this.server.to('users-list').emit('users:list-updated', users);
    this.logger.log('Evento users:list-updated emitido');
  }

  /** Emite apenas para o socket do usuário (rota users/me) quando seus dados são atualizados. */
  emitUserMeUpdated(userId: string, user: any) {
    this.server.to(`user:${userId}`).emit('user:me:updated', user);
    this.logger.log(`Evento user:me:updated emitido para user:${userId}`);
  }
}
