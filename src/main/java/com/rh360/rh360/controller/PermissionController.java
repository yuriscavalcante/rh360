package com.rh360.rh360.controller;

import java.util.List;
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

import com.rh360.rh360.dto.PermissionRequest;
import com.rh360.rh360.dto.PermissionResponse;
import com.rh360.rh360.entity.Permission;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.service.PermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/permissions")
@Tag(name = "Permissões", description = "Endpoints para gerenciamento de permissões")
public class PermissionController {

    private final PermissionService service;

    public PermissionController(PermissionService service) {
        this.service = service;
    }

    @Operation(
        summary = "Criar nova permissão",
        description = "Cria uma nova permissão para um usuário. A função deve ser única para cada usuário."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissão criada com sucesso",
            content = @Content(schema = @Schema(implementation = PermissionResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - dados da permissão incorretos ou faltando",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflito - permissão com esta função já existe para este usuário",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public PermissionResponse create(@RequestBody PermissionRequest permissionRequest) {
        Permission permission = new Permission();
        permission.setFunction(permissionRequest.getFunction());
        permission.setIsPermitted(permissionRequest.getIsPermitted());
        
        User user = new User();
        user.setId(permissionRequest.getUserId());
        permission.setUser(user);
        
        Permission createdPermission = service.create(permission);
        return new PermissionResponse(createdPermission);
    }

    @Operation(
        summary = "Listar todas as permissões",
        description = "Retorna uma lista paginada com todas as permissões cadastradas. " +
                      "Parâmetros de query aceitos: " +
                      "'page' (número da página, começa em 0, padrão: 0), " +
                      "'size' (tamanho da página, padrão: 20), " +
                      "'sort' (campo de ordenação no formato 'campo,direção', exemplo: 'function,asc' ou 'createdAt,desc'), " +
                      "'search' (busca parcial na função da permissão, exemplo: 'create' retorna todas as permissões que contenham 'create' na função).",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de permissões retornada com sucesso",
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
    public Page<PermissionResponse> findAll(Pageable pageable, @RequestParam(value = "search", required = false) String search) {
        return service.findAll(pageable, search);
    }

    @Operation(
        summary = "Buscar permissões por ID do usuário",
        description = "Retorna todas as permissões de um usuário específico",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de permissões retornada com sucesso",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
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
    @GetMapping("/user/{userId}")
    public List<PermissionResponse> findByUserId(@PathVariable UUID userId) {
        return service.findByUserId(userId);
    }

    @Operation(
        summary = "Buscar permissão por ID",
        description = "Retorna os dados de uma permissão específica pelo seu ID",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissão encontrada",
            content = @Content(schema = @Schema(implementation = PermissionResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Permissão não encontrada",
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
    public PermissionResponse findById(@PathVariable UUID id) {
        Permission permission = service.findById(id);
        if (permission == null) {
            throw new RuntimeException("Permissão não encontrada");
        }
        return new PermissionResponse(permission);
    }

    @Operation(
        summary = "Atualizar permissão",
        description = "Atualiza os dados de uma permissão específica. A função deve ser única para cada usuário.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissão atualizada com sucesso",
            content = @Content(schema = @Schema(implementation = PermissionResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Permissão ou usuário não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos - requisição malformada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflito - permissão com esta função já existe para este usuário",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public PermissionResponse update(@PathVariable UUID id, @RequestBody PermissionRequest permissionRequest) {
        Permission permission = new Permission();
        permission.setFunction(permissionRequest.getFunction());
        permission.setIsPermitted(permissionRequest.getIsPermitted());
        
        if (permissionRequest.getUserId() != null) {
            User user = new User();
            user.setId(permissionRequest.getUserId());
            permission.setUser(user);
        }
        
        Permission updatedPermission = service.update(id, permission);
        return new PermissionResponse(updatedPermission);
    }

    @Operation(
        summary = "Deletar permissão",
        description = "Realiza soft delete de uma permissão específica (marca como deletada)",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissão deletada com sucesso"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Permissão não encontrada",
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
