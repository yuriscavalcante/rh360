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

import com.rh360.rh360.dto.ExpenseRequest;
import com.rh360.rh360.dto.ExpenseResponse;
import com.rh360.rh360.service.ExpenseService;
import com.rh360.rh360.util.SecurityUtil;
import com.rh360.rh360.util.DateUtil;

import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/finance/expenses")
@Tag(name = "Financeiro - Despesas", description = "Endpoints para gastos e despesas diversas")
public class FinanceExpensesController {

    private final ExpenseService expenseService;

    public FinanceExpensesController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Operation(summary = "Criar despesa do usuário atual", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/me")
    public ResponseEntity<?> createMe(@RequestBody ExpenseRequest request, HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        try {
            return ResponseEntity.ok(expenseService.create(userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Criar despesa do usuário atual (multipart com anexos)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping(value = "/me", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createMeMultipart(
            @RequestParam("date") String date,
            @RequestParam("amount") String amount,
            @RequestParam("description") String description,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "vendor", required = false) String vendor,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "files", required = false) java.util.List<MultipartFile> files,
            HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        try {
            ExpenseRequest req = new ExpenseRequest();
            req.setDate(DateUtil.parseFlexibleDate(date));
            req.setAmount(new java.math.BigDecimal(amount));
            req.setDescription(description);
            req.setCategory(category);
            req.setPaymentMethod(paymentMethod);
            req.setVendor(vendor);
            req.setStatus(status);
            return ResponseEntity.ok(expenseService.create(userId, req, files));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Listar despesas do usuário atual", security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping("/me")
    public ResponseEntity<?> listMe(
            @RequestParam(value = "from", required = false) LocalDate from,
            @RequestParam(value = "to", required = false) LocalDate to,
            @RequestParam(value = "category", required = false) String category,
            HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        List<ExpenseResponse> result = expenseService.list(userId, from, to, category);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Buscar despesa do usuário atual por ID", security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping("/me/{expenseId}")
    public ResponseEntity<?> findByIdMe(@PathVariable UUID expenseId, HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        try {
            return ResponseEntity.ok(expenseService.findById(expenseId, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Atualizar despesa do usuário atual", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/me/{expenseId}")
    public ResponseEntity<?> updateMe(
            @PathVariable UUID expenseId,
            @RequestBody ExpenseRequest request,
            HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        try {
            return ResponseEntity.ok(expenseService.update(expenseId, userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Atualizar despesa do usuário atual (multipart com anexos)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping(value = "/me/{expenseId}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateMeMultipart(
            @PathVariable UUID expenseId,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "vendor", required = false) String vendor,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "files", required = false) java.util.List<MultipartFile> files,
            HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        try {
            ExpenseRequest req = new ExpenseRequest();
            if (date != null) req.setDate(DateUtil.parseFlexibleDate(date));
            if (amount != null) req.setAmount(new java.math.BigDecimal(amount));
            if (description != null) req.setDescription(description);
            req.setCategory(category);
            req.setPaymentMethod(paymentMethod);
            req.setVendor(vendor);
            req.setStatus(status);
            return ResponseEntity.ok(expenseService.update(expenseId, userId, req, files));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Deletar despesa do usuário atual (soft delete)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @DeleteMapping("/me/{expenseId}")
    public ResponseEntity<?> deleteMe(@PathVariable UUID expenseId, HttpServletRequest httpRequest) {
        UUID userId = SecurityUtil.getUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Usuário não autenticado\"}");
        }
        try {
            expenseService.delete(expenseId, userId);
            return ResponseEntity.ok("{\"success\":true}");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "ADMIN: criar despesa para um usuário", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/users/{userId}")
    public ResponseEntity<?> createForUser(
            @PathVariable UUID userId,
            @RequestBody ExpenseRequest request,
            HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(expenseService.create(userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Criar despesa para um usuário (multipart com anexos)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping(value = "/users/{userId}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createForUserMultipart(
            @PathVariable UUID userId,
            @RequestParam("date") String date,
            @RequestParam("amount") String amount,
            @RequestParam("description") String description,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "vendor", required = false) String vendor,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "files", required = false) java.util.List<MultipartFile> files,
            HttpServletRequest httpRequest) {
        try {
            ExpenseRequest req = new ExpenseRequest();
            req.setDate(DateUtil.parseFlexibleDate(date));
            req.setAmount(new java.math.BigDecimal(amount));
            req.setDescription(description);
            req.setCategory(category);
            req.setPaymentMethod(paymentMethod);
            req.setVendor(vendor);
            req.setStatus(status);
            return ResponseEntity.ok(expenseService.create(userId, req, files));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "ADMIN: listar despesas de um usuário", security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> listForUser(
            @PathVariable UUID userId,
            @RequestParam(value = "from", required = false) LocalDate from,
            @RequestParam(value = "to", required = false) LocalDate to,
            @RequestParam(value = "category", required = false) String category,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(expenseService.list(userId, from, to, category));
    }
}

