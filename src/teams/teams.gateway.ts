import {
  WebSocketGateway,
  WebSocketServer,
  OnGatewayConnection,
  OnGatewayDisconnect,
} from '@nestjs/websockets';
import { Logger } from '@nestjs/common';
import { Server, Socket } from 'socket.io';

@WebSocketGateway({
  cors: { origin: '*' },
  namespace: '/teams',
})
export class TeamsGateway implements OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer()
  server: Server;

  private logger = new Logger('TeamsGateway');

  handleConnection(client: Socket) {
    client.join('teams-list');
    this.logger.log(`[WebSocket] Cliente conectado ao namespace /teams: ${client.id}`);
  }

  handleDisconnect(client: Socket) {
    this.logger.log(`[WebSocket] Cliente desconectado do namespace /teams: ${client.id}`);
  }

  emitTeamCreated(team: any) {
    this.server.to('teams-list').emit('team:created', team);
    this.logger.log(`Evento team:created emitido (${team.id})`);
  }

  emitTeamUpdated(team: any) {
    this.server.to('teams-list').emit('team:updated', team);
    this.logger.log(`Evento team:updated emitido (${team.id})`);
  }

  emitTeamDeleted(teamId: string) {
    this.server.to('teams-list').emit('team:deleted', { id: teamId });
    this.logger.log(`Evento team:deleted emitido (${teamId})`);
  }

  emitTeamsListUpdated() {
    this.server.to('teams-list').emit('teams:list-updated');
    this.logger.log('Evento teams:list-updated emitido');
  }

  /** Emitido quando a lista de usuários de uma equipe muda (add/remove membro). */
  emitTeamUsersUpdated(teamId: string) {
    this.server.to('teams-list').emit('team:users-updated', { teamId });
    this.logger.log(`Evento team:users-updated emitido (teamId: ${teamId})`);
  }
}
