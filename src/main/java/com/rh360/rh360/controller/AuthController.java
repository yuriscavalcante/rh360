package com.rh360.rh360.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rh360.rh360.dto.LoginRequest;
import com.rh360.rh360.dto.LoginResponse;
import com.rh360.rh360.service.AuthService;
import com.rh360.rh360.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e gerenciamento de sessão")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @Operation(
        summary = "Realizar login",
        description = "Autentica um usuário com email e senha, retorna um token JWT válido. " +
                      "Todos os tokens anteriores do usuário são invalidados ao fazer login."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login realizado com sucesso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciais inválidas - usuário não encontrado, senha incorreta ou usuário inativo",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - dados de login faltando ou malformados",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(
        summary = "Realizar logout",
        description = "Invalida o token JWT do usuário autenticado. " +
                      "O token deve ser enviado no header 'Authorization' no formato 'Bearer {token}'.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Logout realizado com sucesso - token invalidado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token não fornecido, formato inválido, expirado ou já invalidado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\":\"Token não fornecido ou formato inválido\"}");
            }

            String token = authHeader.substring(7); // Remove "Bearer "
            authService.logout(token);
            return ResponseEntity.ok("{\"message\":\"Logout realizado com sucesso\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Erro ao realizar logout: " + e.getMessage() + "\"}");
        }
    }

    @Operation(
        summary = "Validar token",
        description = "Valida se o token JWT fornecido no header Authorization é válido. " +
                      "Retorna true se o token for válido e ativo, false caso contrário. " +
                      "O token deve ser enviado no header 'Authorization' no formato 'Bearer {token}'."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Validação realizada com sucesso",
            content = @Content(schema = @Schema(implementation = Boolean.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Token não fornecido ou formato inválido",
            content = @Content
        )
    })
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(false);
        }

        try {
            String token = authHeader.substring(7); // Remove "Bearer "
            Boolean isValid = tokenService.validateToken(token);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}
