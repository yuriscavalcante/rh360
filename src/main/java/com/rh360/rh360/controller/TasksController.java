package com.rh360.rh360.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rh360.rh360.dto.TaskRequest;
import com.rh360.rh360.dto.TaskResponse;
import com.rh360.rh360.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tarefas", description = "Endpoints para gerenciamento de tarefas")
public class TasksController {

    private final TaskService service;

    public TasksController(TaskService service) {
        this.service = service;
    }

    @Operation(
        summary = "Criar nova tarefa",
        description = "Cria uma nova tarefa no sistema. Pode incluir usuário responsável, equipe e tarefa pai para criar subtarefas.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tarefa criada com sucesso",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - dados da tarefa incorretos ou faltando",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário, equipe ou tarefa pai não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @PostMapping
    public TaskResponse create(@RequestBody TaskRequest request) {
        return service.create(request);
    }

    @Operation(
        summary = "Listar todas as tarefas",
        description = "Retorna uma lista paginada com todas as tarefas cadastradas. " +
                      "Parâmetros de query aceitos: " +
                      "'page' (número da página, começa em 0, padrão: 0), " +
                      "'size' (tamanho da página, padrão: 20), " +
                      "'sort' (campo de ordenação no formato 'campo,direção', exemplo: 'title,asc' ou 'createdAt,desc').",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de tarefas retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parâmetros de paginação inválidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @GetMapping
    public Page<TaskResponse> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @Operation(
        summary = "Listar tarefas raiz",
        description = "Retorna uma lista paginada com todas as tarefas que não são subtarefas (sem tarefa pai). " +
                      "Parâmetros de query aceitos: " +
                      "'page' (número da página, começa em 0, padrão: 0), " +
                      "'size' (tamanho da página, padrão: 20), " +
                      "'sort' (campo de ordenação no formato 'campo,direção', exemplo: 'title,asc' ou 'createdAt,desc').",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de tarefas raiz retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parâmetros de paginação inválidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @GetMapping("/root")
    public Page<TaskResponse> findRootTasks(Pageable pageable) {
        return service.findRootTasks(pageable);
    }

    @Operation(
        summary = "Listar tarefas por usuário",
        description = "Retorna uma lista paginada com todas as tarefas atribuídas a um usuário específico. " +
                      "Parâmetros de query aceitos: " +
                      "'page' (número da página, começa em 0, padrão: 0), " +
                      "'size' (tamanho da página, padrão: 20), " +
                      "'sort' (campo de ordenação no formato 'campo,direção', exemplo: 'title,asc' ou 'startDate,desc').",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de tarefas retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parâmetros de paginação inválidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @GetMapping("/users/{userId}")
    public Page<TaskResponse> findByUserId(@PathVariable UUID userId, Pageable pageable) {
        return service.findByUserId(userId, pageable);
    }

    @Operation(
        summary = "Listar tarefas por equipe",
        description = "Retorna uma lista paginada com todas as tarefas atribuídas a uma equipe específica. " +
                      "Parâmetros de query aceitos: " +
                      "'page' (número da página, começa em 0, padrão: 0), " +
                      "'size' (tamanho da página, padrão: 20), " +
                      "'sort' (campo de ordenação no formato 'campo,direção', exemplo: 'title,asc' ou 'startDate,desc').",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de tarefas retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parâmetros de paginação inválidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @GetMapping("/teams/{teamId}")
    public Page<TaskResponse> findByTeamId(@PathVariable UUID teamId, Pageable pageable) {
        return service.findByTeamId(teamId, pageable);
    }

    @Operation(
        summary = "Listar subtarefas",
        description = "Retorna uma lista paginada com todas as subtarefas de uma tarefa específica. " +
                      "Parâmetros de query aceitos: " +
                      "'page' (número da página, começa em 0, padrão: 0), " +
                      "'size' (tamanho da página, padrão: 20), " +
                      "'sort' (campo de ordenação no formato 'campo,direção', exemplo: 'title,asc' ou 'startDate,desc').",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de subtarefas retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarefa pai não encontrada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parâmetros de paginação inválidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @GetMapping("/{parentTaskId}/subtasks")
    public Page<TaskResponse> findSubtasks(@PathVariable UUID parentTaskId, Pageable pageable) {
        return service.findSubtasks(parentTaskId, pageable);
    }

    @Operation(
        summary = "Buscar tarefa por ID",
        description = "Retorna os dados de uma tarefa específica pelo seu ID, incluindo subtarefas e tarefa pai",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tarefa encontrada",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarefa não encontrada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public TaskResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @Operation(
        summary = "Atualizar tarefa",
        description = "Atualiza os dados de uma tarefa específica. Todos os campos são opcionais.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tarefa atualizada com sucesso",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarefa, usuário, equipe ou tarefa pai não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos - requisição malformada ou referência circular",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable UUID id, @RequestBody TaskRequest request) {
        return service.update(id, request);
    }

    @Operation(
        summary = "Deletar tarefa",
        description = "Realiza soft delete de uma tarefa específica (marca como deletada)",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tarefa deletada com sucesso"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarefa não encontrada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
