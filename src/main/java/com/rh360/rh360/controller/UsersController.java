package com.rh360.rh360.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rh360.rh360.dto.UserResponse;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.service.UsersService;
import com.rh360.rh360.util.SecurityUtil;

import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UsersController {

    private final UsersService service;

    public UsersController(UsersService service) {
        this.service = service;
    }

    @Operation(
        summary = "Criar novo usuário",
        description = "Cria um novo usuário no sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário criado com sucesso",
            content = @Content(schema = @Schema(implementation = User.class))
        )
    })
    @PostMapping
    public User create(@RequestBody User user) {
        return service.create(user);
    }

    @Operation(
        summary = "Listar todos os usuários",
        description = "Retorna uma lista com todos os usuários cadastrados",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuários retornada com sucesso",
            content = @Content(schema = @Schema(implementation = User.class))
        )
    })
    @GetMapping
    public List<UserResponse> findAll() {
        return service.findAll();
    }

    @Operation(
        summary = "Buscar usuário por ID",
        description = "Retorna os dados de um usuário específico pelo seu ID",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário encontrado",
            content = @Content(schema = @Schema(implementation = User.class))
        )
    })
    @GetMapping("/{id}")
    public User findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @Operation(
        summary = "Obter usuário atual",
        description = "Retorna os dados do usuário autenticado baseado no token JWT",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dados do usuário atual retornados com sucesso",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuário não autenticado"
        )
    })
    @GetMapping("/me")
    public User getCurrentUser(HttpServletRequest request) {
        UUID userId = SecurityUtil.getUserId(request);
        if (userId == null) {
            throw new RuntimeException("Usuário não autenticado");
        }
        return service.findById(userId);
    }

    @Operation(
        summary = "Atualizar usuário",
        description = "Atualiza os dados de um usuário específico",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = User.class))
        )
    })
    @PutMapping("/{id}")
    public User update(@PathVariable UUID id, @RequestBody User user) {
        return service.update(id, user);
    }

    @Operation(
        summary = "Deletar usuário",
        description = "Deleta um usuário específico",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário deletado com sucesso"
        )
    })
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
