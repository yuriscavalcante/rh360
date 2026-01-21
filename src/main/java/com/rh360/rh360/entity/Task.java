package com.rh360.rh360.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Data
@Schema(description = "Entidade que representa uma tarefa do sistema")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "ID único da tarefa", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Column(nullable = false)
    @Schema(description = "Título da tarefa", example = "Implementar módulo de autenticação")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Descrição detalhada da tarefa", example = "Implementar sistema de autenticação com JWT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @Schema(description = "Usuário responsável pela tarefa (opcional)")
    private User responsibleUser;

    @ManyToOne
    @JoinColumn(name = "team_id")
    @Schema(description = "Equipe responsável pela tarefa (opcional)")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "parent_task_id")
    @Schema(description = "Tarefa pai (para subtarefas, opcional)")
    private Task parentTask;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Lista de subtarefas desta tarefa")
    private List<Task> subtasks;

    @Column(name = "start_date")
    @Schema(description = "Data de início da tarefa (opcional)", example = "2024-01-15T08:30:00")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    @Schema(description = "Data de fim da tarefa (opcional)", example = "2024-01-30T18:00:00")
    private LocalDateTime endDate;

    @Schema(description = "Status da tarefa", example = "pending")
    private String status;

    @Schema(description = "Data de criação do registro")
    private String createdAt;

    @Schema(description = "Data da última atualização do registro")
    private String updatedAt;

    @Schema(description = "Data de exclusão do registro (soft delete)")
    private String deletedAt;

}
