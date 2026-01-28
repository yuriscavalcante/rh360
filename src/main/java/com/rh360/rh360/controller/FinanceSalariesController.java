package com.rh360.rh360.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rh360.rh360.dto.SalaryRequest;
import com.rh360.rh360.dto.SalaryResponse;
import com.rh360.rh360.service.SalaryService;
import com.rh360.rh360.util.SecurityUtil;
import com.rh360.rh360.util.DateUtil;

import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/finance/salaries")
@Tag(name = "Financeiro - Salários", description = "Endpoints para salários e pagamentos")
public class FinanceSalariesController {

    private final SalaryService salaryService;

    public FinanceSalariesController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @Operation(
        summary = "Criar salário do usuário atual",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Salário criado com sucesso",
            content = @Content(schema = @Schema(implementation = SalaryResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping("/me")
    public ResponseEntity<?> createMe(@RequestBody SalaryRequest request, HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        try {
            return ResponseEntity.ok(salaryService.create(userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Criar salário do usuário atual (multipart com anexos)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping(value = "/me", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createMeMultipart(
            @RequestParam("referenceMonth") String referenceMonth,
            @RequestParam("grossAmount") String grossAmount,
            @RequestParam(value = "netAmount", required = false) String netAmount,
            @RequestParam(value = "discounts", required = false) String discounts,
            @RequestParam(value = "bonuses", required = false) String bonuses,
            @RequestParam(value = "paidAt", required = false) String paidAt,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "files", required = false) java.util.List<MultipartFile> files,
            HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        try {
            SalaryRequest req = new SalaryRequest();
            req.setReferenceMonth(referenceMonth);
            req.setGrossAmount(new java.math.BigDecimal(grossAmount));
            if (netAmount != null) req.setNetAmount(new java.math.BigDecimal(netAmount));
            if (discounts != null) req.setDiscounts(new java.math.BigDecimal(discounts));
            if (bonuses != null) req.setBonuses(new java.math.BigDecimal(bonuses));
            if (paidAt != null) req.setPaidAt(DateUtil.parseFlexibleDate(paidAt));
            req.setNotes(notes);
            req.setStatus(status);
            return ResponseEntity.ok(salaryService.create(userId, req, files));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(
        summary = "Listar salários do usuário atual",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/me")
    public ResponseEntity<?> listMe(
            @RequestParam(value = "referenceMonth", required = false) String referenceMonth,
            @RequestParam(value = "fromPaidAt", required = false) LocalDate fromPaidAt,
            @RequestParam(value = "toPaidAt", required = false) LocalDate toPaidAt,
            HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        List<SalaryResponse> result = salaryService.list(userId, referenceMonth, fromPaidAt, toPaidAt);
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Buscar salário do usuário atual por ID",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/me/{salaryId}")
    public ResponseEntity<?> findByIdMe(@PathVariable UUID salaryId, HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        try {
            return ResponseEntity.ok(salaryService.findById(salaryId, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(
        summary = "Atualizar salário do usuário atual",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping("/me/{salaryId}")
    public ResponseEntity<?> updateMe(
            @PathVariable UUID salaryId,
            @RequestBody SalaryRequest request,
            HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        try {
            return ResponseEntity.ok(salaryService.update(salaryId, userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Atualizar salário do usuário atual (multipart com anexos)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping(value = "/me/{salaryId}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateMeMultipart(
            @PathVariable UUID salaryId,
            @RequestParam(value = "referenceMonth", required = false) String referenceMonth,
            @RequestParam(value = "grossAmount", required = false) String grossAmount,
            @RequestParam(value = "netAmount", required = false) String netAmount,
            @RequestParam(value = "discounts", required = false) String discounts,
            @RequestParam(value = "bonuses", required = false) String bonuses,
            @RequestParam(value = "paidAt", required = false) String paidAt,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "files", required = false) java.util.List<MultipartFile> files,
            HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        try {
            SalaryRequest req = new SalaryRequest();
            if (referenceMonth != null) req.setReferenceMonth(referenceMonth);
            if (grossAmount != null) req.setGrossAmount(new java.math.BigDecimal(grossAmount));
            if (netAmount != null) req.setNetAmount(new java.math.BigDecimal(netAmount));
            if (discounts != null) req.setDiscounts(new java.math.BigDecimal(discounts));
            if (bonuses != null) req.setBonuses(new java.math.BigDecimal(bonuses));
            if (paidAt != null) req.setPaidAt(DateUtil.parseFlexibleDate(paidAt));
            if (notes != null) req.setNotes(notes);
            if (status != null) req.setStatus(status);
            return ResponseEntity.ok(salaryService.update(salaryId, userId, req, files));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(
        summary = "Deletar salário do usuário atual (soft delete)",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping("/me/{salaryId}")
    public ResponseEntity<?> deleteMe(@PathVariable UUID salaryId, HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        try {
            salaryService.delete(salaryId, userId);
            return ResponseEntity.ok("{\"success\":true}");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(
        summary = "ADMIN: criar salário para um usuário",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/users/{userId}")
    public ResponseEntity<?> createForUser(
            @PathVariable UUID userId,
            @RequestBody SalaryRequest request,
            HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(salaryService.create(userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Criar salário para um usuário (multipart com anexos)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping(value = "/users/{userId}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createForUserMultipart(
            @PathVariable UUID userId,
            @RequestParam("referenceMonth") String referenceMonth,
            @RequestParam("grossAmount") String grossAmount,
            @RequestParam(value = "netAmount", required = false) String netAmount,
            @RequestParam(value = "discounts", required = false) String discounts,
            @RequestParam(value = "bonuses", required = false) String bonuses,
            @RequestParam(value = "paidAt", required = false) String paidAt,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "files", required = false) java.util.List<MultipartFile> files,
            HttpServletRequest httpRequest) {
        try {
            SalaryRequest req = new SalaryRequest();
            req.setReferenceMonth(referenceMonth);
            req.setGrossAmount(new java.math.BigDecimal(grossAmount));
            if (netAmount != null) req.setNetAmount(new java.math.BigDecimal(netAmount));
            if (discounts != null) req.setDiscounts(new java.math.BigDecimal(discounts));
            if (bonuses != null) req.setBonuses(new java.math.BigDecimal(bonuses));
            if (paidAt != null) req.setPaidAt(DateUtil.parseFlexibleDate(paidAt));
            req.setNotes(notes);
            req.setStatus(status);
            return ResponseEntity.ok(salaryService.create(userId, req, files));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(
        summary = "ADMIN: listar salários de um usuário",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> listForUser(
            @PathVariable UUID userId,
            @RequestParam(value = "referenceMonth", required = false) String referenceMonth,
            @RequestParam(value = "fromPaidAt", required = false) LocalDate fromPaidAt,
            @RequestParam(value = "toPaidAt", required = false) LocalDate toPaidAt,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(salaryService.list(userId, referenceMonth, fromPaidAt, toPaidAt));
    }
}

