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
  namespace: '/time-clock',
})
export class TimeClockGateway implements OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer()
  server: Server;

  private logger = new Logger('TimeClockGateway');

  handleConnection(client: Socket) {
    client.join('attendance-list');
    this.logger.log(`[WebSocket] Cliente conectado ao namespace /time-clock: ${client.id}`);
  }

  handleDisconnect(client: Socket) {
    this.logger.log(`[WebSocket] Cliente desconectado do namespace /time-clock: ${client.id}`);
  }

  emitAttendanceCreated(record: any) {
    this.server.to('attendance-list').emit('attendance:created', record);
    this.logger.log(`Evento attendance:created emitido (${record.id})`);
  }

  emitAttendanceListUpdated() {
    this.server.to('attendance-list').emit('attendance:list-updated');
    this.logger.log('Evento attendance:list-updated emitido');
  }
}
