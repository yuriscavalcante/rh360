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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rh360.rh360.dto.TeamRequest;
import com.rh360.rh360.dto.TeamResponse;
import com.rh360.rh360.dto.UserResponse;
import com.rh360.rh360.service.TeamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Equipes", description = "Endpoints para gerenciamento de equipes")
public class TeamsController {

    private final TeamService service;

    public TeamsController(TeamService service) {
        this.service = service;
    }

    @Operation(
        summary = "Criar nova equipe",
        description = "Cria uma nova equipe no sistema. Pode incluir uma lista de IDs de usuários para associar à equipe.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Equipe criada com sucesso",
            content = @Content(schema = @Schema(implementation = TeamResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - dados da equipe incorretos ou faltando",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado (quando fornecido userIds)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @PostMapping
    public TeamResponse create(@RequestBody TeamRequest request) {
        return service.create(request);
    }

    @Operation(
        summary = "Listar todas as equipes",
        description = "Retorna uma lista paginada com todas as equipes cadastradas. " +
                      "Parâmetros de query aceitos: " +
                      "'page' (número da página, começa em 0, padrão: 0), " +
                      "'size' (tamanho da página, padrão: 20), " +
                      "'sort' (campo de ordenação no formato 'campo,direção', exemplo: 'name,asc' ou 'createdAt,desc'), " +
                      "'search' (busca parcial no nome da equipe, exemplo: 'dev' retorna todas as equipes que contenham 'dev' no nome).",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de equipes retornada com sucesso",
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
    public Page<TeamResponse> findAll(Pageable pageable, @RequestParam(value = "search", required = false) String search) {
        return service.findAll(pageable, search);
    }

    @Operation(
        summary = "Listar equipes por usuário",
        description = "Retorna uma lista paginada com todas as equipes às quais um usuário específico pertence. " +
                      "Parâmetros de query aceitos: " +
                      "'page' (número da página, começa em 0, padrão: 0), " +
                      "'size' (tamanho da página, padrão: 20), " +
                      "'sort' (campo de ordenação no formato 'campo,direção', exemplo: 'name,asc' ou 'createdAt,desc').",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de equipes retornada com sucesso",
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
    public Page<TeamResponse> findByUserId(@PathVariable UUID userId, Pageable pageable) {
        return service.findByUserId(userId, pageable);
    }

    @Operation(
        summary = "Buscar equipe por ID",
        description = "Retorna os dados de uma equipe específica pelo seu ID, incluindo a lista de usuários associados",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Equipe encontrada",
            content = @Content(schema = @Schema(implementation = TeamResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Equipe não encontrada",
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
    public TeamResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @Operation(
        summary = "Atualizar equipe",
        description = "Atualiza os dados de uma equipe específica. Se userIds for fornecido, substitui todos os usuários associados.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Equipe atualizada com sucesso",
            content = @Content(schema = @Schema(implementation = TeamResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Equipe ou usuário não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos - requisição malformada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public TeamResponse update(@PathVariable UUID id, @RequestBody TeamRequest request) {
        return service.update(id, request);
    }

    @Operation(
        summary = "Deletar equipe",
        description = "Realiza soft delete de uma equipe específica (marca como deletada) e remove todos os relacionamentos com usuários",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Equipe deletada com sucesso"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Equipe não encontrada",
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

    @Operation(
        summary = "Adicionar usuário à equipe",
        description = "Adiciona um usuário específico a uma equipe",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário adicionado à equipe com sucesso"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Equipe ou usuário não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Usuário já está associado a esta equipe",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @PostMapping("/{teamId}/users/{userId}")
    public void addUserToTeam(@PathVariable UUID teamId, @PathVariable UUID userId) {
        service.addUserToTeam(teamId, userId);
    }

    @Operation(
        summary = "Remover usuário da equipe",
        description = "Remove um usuário específico de uma equipe",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário removido da equipe com sucesso"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Equipe ou usuário não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @DeleteMapping("/{teamId}/users/{userId}")
    public void removeUserFromTeam(@PathVariable UUID teamId, @PathVariable UUID userId) {
        service.removeUserFromTeam(teamId, userId);
    }

    @Operation(
        summary = "Listar usuários de uma equipe",
        description = "Retorna uma lista paginada com todos os usuários associados a uma equipe específica. " +
                      "Parâmetros de query aceitos: " +
                      "'page' (número da página, começa em 0, padrão: 0), " +
                      "'size' (tamanho da página, padrão: 20), " +
                      "'sort' (campo de ordenação no formato 'campo,direção', exemplo: 'name,asc' ou 'email,desc').",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuários retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Equipe não encontrada",
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
    @GetMapping("/{teamId}/users")
    public Page<UserResponse> findUsersByTeamId(@PathVariable UUID teamId, Pageable pageable) {
        return service.findUsersByTeamId(teamId, pageable);
    }
}
