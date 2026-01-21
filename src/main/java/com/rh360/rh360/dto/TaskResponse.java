package com.rh360.rh360.dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.rh360.rh360.entity.Task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Resposta de tarefa")
public class TaskResponse {
    
    @Schema(description = "ID da tarefa", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "Título da tarefa", example = "Implementar módulo de autenticação")
    private String title;
    
    @Schema(description = "Descrição detalhada da tarefa", example = "Implementar sistema de autenticação com JWT")
    private String description;
    
    @Schema(description = "Usuário responsável pela tarefa")
    private UserResponse responsibleUser;
    
    @Schema(description = "Equipe responsável pela tarefa")
    private TeamResponse team;
    
    @Schema(description = "Tarefa pai (para subtarefas)")
    private TaskResponse parentTask;
    
    @Schema(description = "Lista de subtarefas")
    private List<TaskResponse> subtasks;
    
    @Schema(description = "Data de início da tarefa", example = "2024-01-15T08:30:00")
    private LocalDateTime startDate;
    
    @Schema(description = "Data de fim da tarefa", example = "2024-01-30T18:00:00")
    private LocalDateTime endDate;
    
    @Schema(description = "Status da tarefa", example = "pending")
    private String status;
    
    @Schema(description = "Data de criação da tarefa", example = "2021-01-01")
    private String createdAt;
    
    @Schema(description = "Data de atualização da tarefa", example = "2021-01-01")
    private String updatedAt;

    public TaskResponse(Task task) {
        this(task, false, false);
    }
    
    public TaskResponse(Task task, boolean includeSubtasks, boolean includeParent) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.startDate = task.getStartDate();
        this.endDate = task.getEndDate();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
        
        if (task.getResponsibleUser() != null) {
            this.responsibleUser = new UserResponse(task.getResponsibleUser());
        }
        
        if (task.getTeam() != null) {
            this.team = new TeamResponse(task.getTeam(), false);
        }
        
        if (includeParent && task.getParentTask() != null) {
            this.parentTask = new TaskResponse(task.getParentTask(), false, false);
        }
        
        if (includeSubtasks && task.getSubtasks() != null && !task.getSubtasks().isEmpty()) {
            this.subtasks = task.getSubtasks().stream()
                .map(subtask -> new TaskResponse(subtask, false, false))
                .collect(Collectors.toList());
        } else {
            this.subtasks = List.of();
        }
    }
}
