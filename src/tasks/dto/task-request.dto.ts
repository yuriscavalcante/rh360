export class TaskRequest {
  title?: string;
  description?: string;
  responsibleUserId?: string;
  teamId?: string;
  parentTaskId?: string;
  startDate?: Date;
  endDate?: Date;
  status?: string;
}
