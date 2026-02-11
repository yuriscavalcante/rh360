import {
  WebSocketGateway,
  WebSocketServer,
  OnGatewayConnection,
  OnGatewayDisconnect,
} from '@nestjs/websockets';
import { Logger } from '@nestjs/common';
import { Server, Socket } from 'socket.io';

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

  handleConnection(client: Socket) {
    this.logger.log(`Cliente conectado: ${client.id}`);
    client.join('users-list');
  }

  handleDisconnect(client: Socket) {
    this.logger.log(`Cliente desconectado: ${client.id}`);
  }

  // Método para emitir evento quando um usuário é criado
  emitUserCreated(user: any) {
    this.server.to('users-list').emit('user:created', user);
    this.logger.log(`Evento user:created emitido para ${user.id}`);
  }

  // Método para emitir evento quando um usuário é atualizado
  emitUserUpdated(user: any) {
    this.server.to('users-list').emit('user:updated', user);
    this.logger.log(`Evento user:updated emitido para ${user.id}`);
  }

  // Método para emitir evento quando um usuário é removido
  emitUserDeleted(userId: string) {
    this.server.to('users-list').emit('user:deleted', { id: userId });
    this.logger.log(`Evento user:deleted emitido para ${userId}`);
  }

  // Método para emitir atualização completa da lista
  emitUsersListUpdated(users: any) {
    this.server.to('users-list').emit('users:list-updated', users);
    this.logger.log('Evento users:list-updated emitido');
  }
}
