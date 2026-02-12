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
    // Sala da listagem geral: quem conecta aqui recebe create/update/delete em tempo real
    client.join('users-list');
  }

  handleDisconnect(client: Socket) {
    this.logger.log(`Cliente desconectado: ${client.id}`);
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
}
