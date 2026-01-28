package com.rh360.rh360.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rh360.rh360.dto.FinanceSummaryResponse;
import com.rh360.rh360.service.FinanceSummaryService;
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
@RequestMapping("/api/finance")
@Tag(name = "Financeiro - Resumo", description = "Resumo financeiro por período")
public class FinanceSummaryController {

    private final FinanceSummaryService financeSummaryService;

    public FinanceSummaryController(FinanceSummaryService financeSummaryService) {
        this.financeSummaryService = financeSummaryService;
    }

    @Operation(summary = "Resumo mensal do usuário atual", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso",
            content = @Content(schema = @Schema(implementation = FinanceSummaryResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos", content = @Content)
    })
    @GetMapping("/summary/me")
    public ResponseEntity<?> summaryMe(@RequestParam("referenceMonth") String referenceMonth, HttpServletRequest request) {
        UUID userId = SecurityUtil.getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        try {
            return ResponseEntity.ok(financeSummaryService.summaryForMonth(userId, referenceMonth));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "ADMIN: resumo mensal de um usuário", security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping("/summary/users/{userId}")
    public ResponseEntity<?> summaryForUser(
            @PathVariable UUID userId,
            @RequestParam("referenceMonth") String referenceMonth,
            HttpServletRequest request) {
        try {
            return ResponseEntity.ok(financeSummaryService.summaryForMonth(userId, referenceMonth));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}

