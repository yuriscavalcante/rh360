import { UserResponseDto } from '../../users/dto/user-response.dto';
import { TeamResponse } from '../../teams/dto/team-response.dto';

export class TaskResponse {
  id: string;
  title: string;
  description?: string;
  responsibleUser?: UserResponseDto;
  team?: TeamResponse;
  parentTask?: TaskResponse;
  subtasks?: TaskResponse[];
  startDate?: Date;
  endDate?: Date;
  status?: string;
  createdAt: string;
  updatedAt: string;
}
