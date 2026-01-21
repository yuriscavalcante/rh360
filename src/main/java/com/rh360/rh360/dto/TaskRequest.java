package com.rh360.rh360.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Requisição para criar ou atualizar uma tarefa")
public class TaskRequest {
    
    @Schema(description = "Título da tarefa", example = "Implementar módulo de autenticação")
    private String title;
    
    @Schema(description = "Descrição detalhada da tarefa", example = "Implementar sistema de autenticação com JWT")
    private String description;
    
    @Schema(description = "ID do usuário responsável pela tarefa (opcional)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID responsibleUserId;
    
    @Schema(description = "ID da equipe responsável pela tarefa (opcional)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID teamId;
    
    @Schema(description = "ID da tarefa pai (para subtarefas, opcional)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID parentTaskId;
    
    @Schema(description = "Data de início da tarefa (opcional)", example = "2024-01-15T08:30:00")
    private LocalDateTime startDate;
    
    @Schema(description = "Data de fim da tarefa (opcional)", example = "2024-01-30T18:00:00")
    private LocalDateTime endDate;
    
    @Schema(description = "Status da tarefa", example = "pending")
    private String status;
}
