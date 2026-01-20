package com.rh360.rh360.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rh360.rh360.service.CompreFaceService;
import com.rh360.rh360.service.CompreFaceService.FaceVerifyResponse;
import com.rh360.rh360.service.UsersService;
import com.rh360.rh360.entity.User;
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
@RequestMapping("/api/faces")
@Tag(name = "Validação Facial", description = "Endpoints para validação e registro de faces usando CompreFace")
public class FaceController {

    private final CompreFaceService compreFaceService;
    private final UsersService usersService;

    public FaceController(CompreFaceService compreFaceService, UsersService usersService) {
        this.compreFaceService = compreFaceService;
        this.usersService = usersService;
    }

    @Operation(
        summary = "Validar face do usuário",
        description = "Valida se a foto enviada corresponde ao rosto cadastrado do usuário. " +
                      "Retorna o resultado da verificação com nível de confiança.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Validação realizada com sucesso",
            content = @Content(schema = @Schema(implementation = FaceVerifyResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - arquivo de imagem ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor ou erro na comunicação com CompreFace",
            content = @Content
        )
    })
    @PostMapping("/{userId}/verify")
    public ResponseEntity<?> verifyFace(
            @PathVariable UUID userId,
            @RequestParam("photo") MultipartFile photo,
            HttpServletRequest request) {
        
        // Verificar se o token pertence ao mesmo usuário do userId
        UUID tokenUserId = SecurityUtil.getUserId(request);
        if (tokenUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Usuário não autenticado\"}");
        }
        
        // Validar que o token pertence ao mesmo usuário do path
        if (!tokenUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("{\"error\":\"Você não tem permissão para validar a face deste usuário\"}");
        }
        
        // Verificar se o usuário existe
        User user = usersService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\":\"Usuário não encontrado\"}");
        }

        // Verificar se o arquivo foi enviado
        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\":\"Arquivo de imagem é obrigatório\"}");
        }

        try {
            // Verificar face usando o CompreFaceService
            FaceVerifyResponse response = compreFaceService.verifyFace(userId, photo);

            if (response == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Erro ao verificar face no CompreFace\"}");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Erro ao processar validação facial: " + e.getMessage() + "\"}");
        }
    }

    @Operation(
        summary = "Validar face do usuário atual",
        description = "Valida se a foto enviada corresponde ao rosto cadastrado do usuário autenticado. " +
                      "O ID do usuário é extraído automaticamente do token JWT.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Validação realizada com sucesso",
            content = @Content(schema = @Schema(implementation = FaceVerifyResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - arquivo de imagem ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor ou erro na comunicação com CompreFace",
            content = @Content
        )
    })
    @PostMapping("/me/verify")
    public ResponseEntity<?> verifyMyFace(
            @RequestParam("photo") MultipartFile photo,
            HttpServletRequest request) {
        
        UUID userId = SecurityUtil.getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Usuário não autenticado\"}");
        }

        return verifyFace(userId, photo, request);
    }

    @Operation(
        summary = "Registrar face do usuário",
        description = "Registra uma nova face no CompreFace para o usuário especificado. " +
                      "Útil para atualizar ou adicionar uma face caso não tenha sido registrada no cadastro.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Face registrada com sucesso",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - arquivo de imagem ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor ou erro na comunicação com CompreFace",
            content = @Content
        )
    })
    @PostMapping("/{userId}/register")
    public ResponseEntity<?> registerFace(
            @PathVariable UUID userId,
            @RequestParam("photo") MultipartFile photo,
            HttpServletRequest request) {
        
        // Verificar se o token pertence ao mesmo usuário do userId
        UUID tokenUserId = SecurityUtil.getUserId(request);
        if (tokenUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Usuário não autenticado\"}");
        }
        
        // Validar que o token pertence ao mesmo usuário do path
        if (!tokenUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("{\"error\":\"Você não tem permissão para registrar a face deste usuário\"}");
        }
        
        // Verificar se o usuário existe
        User user = usersService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\":\"Usuário não encontrado\"}");
        }

        // Verificar se o arquivo foi enviado
        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\":\"Arquivo de imagem é obrigatório\"}");
        }

        try {
            // Registrar face usando o CompreFaceService
            boolean success = compreFaceService.addFace(userId, photo);

            if (success) {
                return ResponseEntity.ok("{\"message\":\"Face registrada com sucesso no CompreFace\"}");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Falha ao registrar face no CompreFace\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Erro ao processar registro facial: " + e.getMessage() + "\"}");
        }
    }

    @Operation(
        summary = "Registrar face do usuário atual",
        description = "Registra uma nova face no CompreFace para o usuário autenticado. " +
                      "O ID do usuário é extraído automaticamente do token JWT.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Face registrada com sucesso",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - arquivo de imagem ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor ou erro na comunicação com CompreFace",
            content = @Content
        )
    })
    @PostMapping("/me/register")
    public ResponseEntity<?> registerMyFace(
            @RequestParam("photo") MultipartFile photo,
            HttpServletRequest request) {
        
        UUID userId = SecurityUtil.getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Usuário não autenticado\"}");
        }

        return registerFace(userId, photo, request);
    }

    @Operation(
        summary = "Validar face usando URL",
        description = "Valida se a foto na URL fornecida corresponde ao rosto cadastrado do usuário. " +
                      "Útil quando a imagem já está armazenada (ex: no Cloudflare R2).",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Validação realizada com sucesso",
            content = @Content(schema = @Schema(implementation = FaceVerifyResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - URL da imagem ausente ou inválida",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor ou erro na comunicação com CompreFace",
            content = @Content
        )
    })
    @PostMapping("/{userId}/verify-url")
    public ResponseEntity<?> verifyFaceFromUrl(
            @PathVariable UUID userId,
            @RequestParam("photoUrl") String photoUrl,
            HttpServletRequest request) {
        
        // Verificar se o token pertence ao mesmo usuário do userId
        UUID tokenUserId = SecurityUtil.getUserId(request);
        if (tokenUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Usuário não autenticado\"}");
        }
        
        // Validar que o token pertence ao mesmo usuário do path
        if (!tokenUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("{\"error\":\"Você não tem permissão para validar a face deste usuário\"}");
        }
        
        // Verificar se o usuário existe
        User user = usersService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\":\"Usuário não encontrado\"}");
        }

        // Verificar se a URL foi fornecida
        if (photoUrl == null || photoUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\":\"URL da imagem é obrigatória\"}");
        }

        try {
            // Verificar face usando o CompreFaceService
            FaceVerifyResponse response = compreFaceService.verifyFaceFromUrl(userId, photoUrl);

            if (response == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Erro ao verificar face no CompreFace\"}");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Erro ao processar validação facial: " + e.getMessage() + "\"}");
        }
    }

    @Operation(
        summary = "Registrar face usando URL",
        description = "Registra uma face no CompreFace usando a URL da imagem. " +
                      "Útil quando a imagem já está armazenada (ex: no Cloudflare R2).",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Face registrada com sucesso",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - URL da imagem ausente ou inválida",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor ou erro na comunicação com CompreFace",
            content = @Content
        )
    })
    @PostMapping("/{userId}/register-url")
    public ResponseEntity<?> registerFaceFromUrl(
            @PathVariable UUID userId,
            @RequestParam("photoUrl") String photoUrl,
            HttpServletRequest request) {
        
        // Verificar se o token pertence ao mesmo usuário do userId
        UUID tokenUserId = SecurityUtil.getUserId(request);
        if (tokenUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Usuário não autenticado\"}");
        }
        
        // Validar que o token pertence ao mesmo usuário do path
        if (!tokenUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("{\"error\":\"Você não tem permissão para registrar a face deste usuário\"}");
        }
        
        // Verificar se o usuário existe
        User user = usersService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\":\"Usuário não encontrado\"}");
        }

        // Verificar se a URL foi fornecida
        if (photoUrl == null || photoUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\":\"URL da imagem é obrigatória\"}");
        }

        try {
            // Registrar face usando o CompreFaceService
            boolean success = compreFaceService.addFaceFromUrl(userId, photoUrl);

            if (success) {
                return ResponseEntity.ok("{\"message\":\"Face registrada com sucesso no CompreFace\"}");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Falha ao registrar face no CompreFace\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Erro ao processar registro facial: " + e.getMessage() + "\"}");
        }
    }
}
