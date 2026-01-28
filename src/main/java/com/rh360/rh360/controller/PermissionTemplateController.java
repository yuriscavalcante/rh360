package com.rh360.rh360.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rh360.rh360.dto.PermissionTemplateRequest;
import com.rh360.rh360.dto.PermissionTemplateResponse;
import com.rh360.rh360.service.PermissionTemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/permission-templates")
@Tag(name = "Templates de Permissões", description = "Endpoints para gerenciamento de templates de permissões")
public class PermissionTemplateController {

    private final PermissionTemplateService service;

    public PermissionTemplateController(PermissionTemplateService service) {
        this.service = service;
    }

    @Operation(
        summary = "Criar novo template de permissão",
        description = "Cria um novo template de permissão no sistema. Os campos 'nome', 'label' e 'rota' são obrigatórios.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Template de permissão criado com sucesso",
            content = @Content(schema = @Schema(implementation = PermissionTemplateResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - dados do template incorretos ou faltando",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @PostMapping
    public PermissionTemplateResponse create(@RequestBody PermissionTemplateRequest request) {
        return service.create(request);
    }

    @Operation(
        summary = "Listar todos os templates de permissão",
        description = "Retorna uma lista com todos os templates de permissão cadastrados no sistema. " +
                      "Parâmetros de query aceitos: " +
                      "'search' (busca parcial no nome ou label do template, exemplo: 'user' retorna todos os templates que contenham 'user' no nome ou label).",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de templates de permissão retornada com sucesso",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @GetMapping
    public List<PermissionTemplateResponse> findAll(@RequestParam(value = "search", required = false) String search) {
        return service.findAll(search);
    }
}
