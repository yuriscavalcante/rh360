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
  namespace: '/tasks',
})
export class TasksGateway implements OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer()
  server: Server;

  private logger = new Logger('TasksGateway');

  handleConnection(client: Socket) {
    client.join('tasks-list');
    this.logger.log(`[WebSocket] Cliente conectado ao namespace /tasks: ${client.id}`);
  }

  handleDisconnect(client: Socket) {
    this.logger.log(`[WebSocket] Cliente desconectado do namespace /tasks: ${client.id}`);
  }

  emitTaskCreated(task: any) {
    this.server.to('tasks-list').emit('task:created', task);
    this.logger.log(`Evento task:created emitido (${task.id})`);
  }

  emitTaskUpdated(task: any) {
    this.server.to('tasks-list').emit('task:updated', task);
    this.logger.log(`Evento task:updated emitido (${task.id})`);
  }

  emitTaskDeleted(taskId: string) {
    this.server.to('tasks-list').emit('task:deleted', { id: taskId });
    this.logger.log(`Evento task:deleted emitido (${taskId})`);
  }

  emitTasksListUpdated() {
    this.server.to('tasks-list').emit('tasks:list-updated');
    this.logger.log('Evento tasks:list-updated emitido');
  }
}
