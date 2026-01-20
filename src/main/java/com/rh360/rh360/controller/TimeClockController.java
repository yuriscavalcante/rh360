package com.rh360.rh360.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rh360.rh360.dto.QrCodeResponse;
import com.rh360.rh360.dto.TimeClockResponse;
import com.rh360.rh360.service.QrCodeService;
import com.rh360.rh360.service.TimeClockService;
import com.rh360.rh360.service.TokenService;
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
@RequestMapping("/api/timeclock")
@Tag(name = "Bater Ponto", description = "Endpoints para registro de ponto com validação facial")
public class TimeClockController {

    private final TimeClockService timeClockService;
    private final UsersService usersService;
    private final QrCodeService qrCodeService;
    private final TokenService tokenService;

    public TimeClockController(TimeClockService timeClockService, UsersService usersService, 
                              QrCodeService qrCodeService, TokenService tokenService) {
        this.timeClockService = timeClockService;
        this.usersService = usersService;
        this.qrCodeService = qrCodeService;
        this.tokenService = tokenService;
    }

    @Operation(
        summary = "Bater ponto",
        description = "Registra um ponto para o usuário especificado após validar sua face através do reconhecimento facial. " +
                      "A foto enviada será comparada com a face cadastrada do usuário. " +
                      "O ponto só será registrado se a validação facial for bem-sucedida com confiança mínima de 70%.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Ponto registrado com sucesso",
            content = @Content(schema = @Schema(implementation = TimeClockResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - arquivo de imagem ausente, inválido ou face não validada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado - token não pertence ao usuário especificado",
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
    @PostMapping("/{userId}")
    public ResponseEntity<?> clockIn(
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
                .body("{\"error\":\"Você não tem permissão para bater ponto para este usuário\"}");
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
            // Bater ponto (valida a face internamente)
            TimeClockResponse response = timeClockService.clockIn(userId, photo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Erro de validação facial ou usuário não encontrado
            if (e.getMessage().contains("não encontrado") || e.getMessage().contains("obrigatória")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
            } else if (e.getMessage().contains("não foi validada") || e.getMessage().contains("Face não validada")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Erro ao processar registro de ponto: " + e.getMessage() + "\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Erro ao processar registro de ponto: " + e.getMessage() + "\"}");
        }
    }

    @Operation(
        summary = "Bater ponto para o usuário atual",
        description = "Registra um ponto para o usuário autenticado após validar sua face. " +
                      "O ID do usuário é extraído automaticamente do token JWT. " +
                      "A foto enviada será comparada com a face cadastrada do usuário. " +
                      "O ponto só será registrado se a validação facial for bem-sucedida com confiança mínima de 70%.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Ponto registrado com sucesso",
            content = @Content(schema = @Schema(implementation = TimeClockResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - arquivo de imagem ausente, inválido ou face não validada",
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
    @PostMapping("/me")
    public ResponseEntity<?> clockInMe(
            @RequestParam("photo") MultipartFile photo,
            HttpServletRequest request) {
        
        UUID userId = SecurityUtil.getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Usuário não autenticado\"}");
        }

        return clockIn(userId, photo, request);
    }

    @Operation(
        summary = "Listar pontos de um usuário",
        description = "Retorna todos os registros de ponto de um usuário específico, ordenados por data/hora (mais recentes primeiro). " +
                      "O usuário só pode visualizar seus próprios pontos, exceto se for administrador.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de pontos retornada com sucesso",
            content = @Content(schema = @Schema(implementation = TimeClockResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado - token JWT ausente ou inválido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado - tentativa de visualizar pontos de outro usuário",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTimeClocksByUserId(
            @PathVariable UUID userId,
            HttpServletRequest request) {
        
        // Verificar se o usuário está autenticado
        UUID tokenUserId = SecurityUtil.getUserId(request);
        if (tokenUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Usuário não autenticado\"}");
        }
        
        // Verificar se o token pertence ao mesmo usuário do path
        // TODO: Permitir que admins vejam pontos de outros usuários
        if (!tokenUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("{\"error\":\"Você não tem permissão para visualizar pontos deste usuário\"}");
        }

        try {
            List<TimeClockResponse> timeClocks = timeClockService.findByUserId(userId);
            return ResponseEntity.ok(timeClocks);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Erro ao buscar registros de ponto: " + e.getMessage() + "\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Erro ao buscar registros de ponto: " + e.getMessage() + "\"}");
        }
    }

    @Operation(
        summary = "Listar pontos do usuário atual",
        description = "Retorna todos os registros de ponto do usuário autenticado, ordenados por data/hora (mais recentes primeiro). " +
                      "O ID do usuário é extraído automaticamente do token JWT.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de pontos retornada com sucesso",
            content = @Content(schema = @Schema(implementation = TimeClockResponse.class))
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
            description = "Erro interno do servidor",
            content = @Content
        )
    })
    @GetMapping("/me")
    public ResponseEntity<?> getMyTimeClocks(HttpServletRequest request) {
        UUID userId = SecurityUtil.getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Usuário não autenticado\"}");
        }

        return getTimeClocksByUserId(userId, request);
    }

    @Operation(
        summary = "Gerar QR code para bater ponto via mobile",
        description = "Gera um QR code com um token temporário que permite ao usuário bater ponto através do celular. " +
                      "O QR code contém uma URL que abre a câmera do celular para capturar a foto e bater o ponto. " +
                      "O token do QR code expira em 15 minutos por segurança.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "QR code gerado com sucesso",
            content = @Content(schema = @Schema(implementation = QrCodeResponse.class))
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
            description = "Erro interno do servidor ao gerar QR code",
            content = @Content
        )
    })
    @GetMapping("/qr-code")
    public ResponseEntity<?> generateQrCode(HttpServletRequest request) {
        UUID userId = SecurityUtil.getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Usuário não autenticado\"}");
        }

        try {
            // Buscar dados do usuário
            User user = usersService.findById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"Usuário não encontrado\"}");
            }

            String email = SecurityUtil.getEmail(request);
            String role = SecurityUtil.getRole(request);

            // Gerar token temporário para QR code
            String qrToken = tokenService.generateQrCodeToken(userId, email, role);
            tokenService.saveQrCodeToken(qrToken, userId);

            // Gerar URL do QR code
            String qrCodeUrl = qrCodeService.generateQrCodeUrl(qrToken);

            // Gerar QR code em Base64
            String qrCodeBase64 = qrCodeService.generateQrCodeBase64(qrCodeUrl);

            // Calcular tempo de expiração em minutos
            Long expiresInMinutes = tokenService.getQrCodeExpiration() / 60000; // converter ms para minutos

            QrCodeResponse response = new QrCodeResponse();
            response.setQrCodeBase64(qrCodeBase64);
            response.setUrl(qrCodeUrl);
            response.setToken(qrToken);
            response.setExpiresInMinutes(expiresInMinutes);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Erro ao gerar QR code: " + e.getMessage() + "\"}");
        }
    }

    @Operation(
        summary = "Bater ponto via mobile (QR code)",
        description = "Endpoint público para bater ponto usando o token do QR code. " +
                      "Este endpoint permite que o usuário bata ponto através do celular após escanear o QR code. " +
                      "A foto será validada usando reconhecimento facial antes de registrar o ponto.",
        security = @SecurityRequirement(name = "")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Ponto registrado com sucesso",
            content = @Content(schema = @Schema(implementation = TimeClockResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - token inválido, arquivo de imagem ausente ou face não validada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token do QR code inválido ou expirado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor ou erro na comunicação com CompreFace",
            content = @Content
        )
    })
    @PostMapping("/mobile/{qrToken}")
    public ResponseEntity<?> clockInMobile(
            @PathVariable String qrToken,
            @RequestParam("photo") MultipartFile photo) {
        
        try {
            // Validar o token do QR code
            if (!tokenService.validateToken(qrToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Token do QR code inválido ou expirado\"}");
            }

            // Extrair userId do token
            UUID userId = tokenService.extractUserId(qrToken);
            
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

            // Bater ponto (valida a face internamente)
            TimeClockResponse response = timeClockService.clockIn(userId, photo);
            
            // Desativar o token do QR code após uso (opcional, por segurança)
            // tokenService.deactivateToken(qrToken);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Erro de validação facial ou usuário não encontrado
            if (e.getMessage().contains("não encontrado") || e.getMessage().contains("obrigatória")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
            } else if (e.getMessage().contains("não foi validada") || e.getMessage().contains("Face não validada")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Erro ao processar registro de ponto: " + e.getMessage() + "\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Erro ao processar registro de ponto: " + e.getMessage() + "\"}");
        }
    }
}
