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

import com.rh360.rh360.dto.CompanyExpenseRequest;
import com.rh360.rh360.dto.CompanyExpenseResponse;
import com.rh360.rh360.service.CompanyExpenseService;
import com.rh360.rh360.util.DateUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/finance/company-expenses")
@Tag(name = "Financeiro - Gastos Diversos", description = "Endpoints para gastos diversos (empresa) sem vínculo com usuário")
public class CompanyExpensesController {

    private final CompanyExpenseService service;

    public CompanyExpensesController(CompanyExpenseService service) {
        this.service = service;
    }

    @Operation(summary = "Criar gasto diverso (JSON)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping(consumes = {"application/json"})
    public CompanyExpenseResponse create(@RequestBody CompanyExpenseRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Criar gasto diverso (multipart com anexos)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createMultipart(
            @RequestParam("title") String title,
            @RequestParam("type") String type,
            @RequestParam("date") String date,
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        try {
            CompanyExpenseRequest req = new CompanyExpenseRequest();
            req.setTitle(title);
            req.setType(type);
            req.setDate(DateUtil.parseFlexibleDate(date));
            if (amount != null) req.setAmount(new java.math.BigDecimal(amount));
            req.setDescription(description);
            req.setStatus(status);
            return ResponseEntity.ok(service.create(req, files));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Listar gastos diversos", security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping
    public List<CompanyExpenseResponse> list(
            @RequestParam(value = "from", required = false) LocalDate from,
            @RequestParam(value = "to", required = false) LocalDate to,
            @RequestParam(value = "type", required = false) String type) {
        return service.list(from, to, type);
    }

    @Operation(summary = "Buscar gasto diverso por ID", security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping("/{id}")
    public CompanyExpenseResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @Operation(summary = "Atualizar gasto diverso (JSON)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping(value = "/{id}", consumes = {"application/json"})
    public CompanyExpenseResponse update(@PathVariable UUID id, @RequestBody CompanyExpenseRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Atualizar gasto diverso (multipart com anexos)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateMultipart(
            @PathVariable UUID id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        try {
            CompanyExpenseRequest req = new CompanyExpenseRequest();
            req.setTitle(title);
            req.setType(type);
            if (date != null) req.setDate(DateUtil.parseFlexibleDate(date));
            if (amount != null) req.setAmount(new java.math.BigDecimal(amount));
            req.setDescription(description);
            req.setStatus(status);
            return ResponseEntity.ok(service.update(id, req, files));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Deletar gasto diverso (soft delete)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        try {
            service.delete(id);
            return ResponseEntity.ok("{\"success\":true}");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}

