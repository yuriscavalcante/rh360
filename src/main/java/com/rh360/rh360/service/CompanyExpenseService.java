package com.rh360.rh360.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rh360.rh360.dto.CompanyExpenseRequest;
import com.rh360.rh360.dto.CompanyExpenseResponse;
import com.rh360.rh360.entity.CompanyExpense;
import com.rh360.rh360.entity.CompanyExpenseAttachment;
import com.rh360.rh360.repository.CompanyExpenseAttachmentRepository;
import com.rh360.rh360.repository.CompanyExpenseRepository;

@Service
public class CompanyExpenseService {

    private final CompanyExpenseRepository repository;
    private final CompanyExpenseAttachmentRepository attachmentRepository;
    private final R2StorageService r2StorageService;

    public CompanyExpenseService(
            CompanyExpenseRepository repository,
            CompanyExpenseAttachmentRepository attachmentRepository,
            R2StorageService r2StorageService) {
        this.repository = repository;
        this.attachmentRepository = attachmentRepository;
        this.r2StorageService = r2StorageService;
    }

    public CompanyExpenseResponse create(CompanyExpenseRequest request) {
        return create(request, null);
    }

    public CompanyExpenseResponse create(CompanyExpenseRequest request, List<MultipartFile> files) {
        validateRequired(request);

        CompanyExpense expense = new CompanyExpense();
        applyRequest(expense, request);
        expense.setCreatedAt(LocalDateTime.now().toString());
        expense.setUpdatedAt(LocalDateTime.now().toString());
        if (expense.getStatus() == null || expense.getStatus().isBlank()) {
            expense.setStatus("paid");
        }

        CompanyExpense saved = repository.save(expense);
        saveAttachments(saved, files);
        return new CompanyExpenseResponse(saved, attachmentRepository.findByCompanyExpense_IdAndDeletedAtIsNull(saved.getId()));
    }

    public CompanyExpenseResponse update(UUID id, CompanyExpenseRequest request) {
        return update(id, request, null);
    }

    public CompanyExpenseResponse update(UUID id, CompanyExpenseRequest request, List<MultipartFile> files) {
        CompanyExpense existing = repository.findById(id).orElse(null);
        if (existing == null || existing.getDeletedAt() != null) {
            throw new RuntimeException("Gasto não encontrado");
        }

        applyRequest(existing, request);
        existing.setUpdatedAt(LocalDateTime.now().toString());
        CompanyExpense saved = repository.save(existing);
        saveAttachments(saved, files);
        return new CompanyExpenseResponse(saved, attachmentRepository.findByCompanyExpense_IdAndDeletedAtIsNull(saved.getId()));
    }

    public void delete(UUID id) {
        CompanyExpense existing = repository.findById(id).orElse(null);
        if (existing == null || existing.getDeletedAt() != null) {
            throw new RuntimeException("Gasto não encontrado");
        }
        existing.setDeletedAt(LocalDateTime.now().toString());
        existing.setUpdatedAt(LocalDateTime.now().toString());
        repository.save(existing);
    }

    public CompanyExpenseResponse findById(UUID id) {
        CompanyExpense existing = repository.findById(id).orElse(null);
        if (existing == null || existing.getDeletedAt() != null) {
            throw new RuntimeException("Gasto não encontrado");
        }
        return new CompanyExpenseResponse(existing, attachmentRepository.findByCompanyExpense_IdAndDeletedAtIsNull(existing.getId()));
    }

    public List<CompanyExpenseResponse> list(LocalDate from, LocalDate to, String type) {
        List<CompanyExpense> expenses = repository.findByFilters(from, to, normalizeNullBlank(type));
        return expenses.stream()
                .map(e -> new CompanyExpenseResponse(e, attachmentRepository.findByCompanyExpense_IdAndDeletedAtIsNull(e.getId())))
                .collect(Collectors.toList());
    }

    private void validateRequired(CompanyExpenseRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("title é obrigatório");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new RuntimeException("type é obrigatório");
        }
        if (request.getDate() == null) {
            throw new RuntimeException("date é obrigatório");
        }
    }

    private void applyRequest(CompanyExpense expense, CompanyExpenseRequest request) {
        if (request.getTitle() != null) expense.setTitle(request.getTitle());
        if (request.getType() != null) expense.setType(request.getType());
        if (request.getDate() != null) expense.setDate(request.getDate());
        if (request.getAmount() != null) expense.setAmount(request.getAmount());
        if (request.getDescription() != null) expense.setDescription(request.getDescription());
        if (request.getStatus() != null) expense.setStatus(request.getStatus());
    }

    private void saveAttachments(CompanyExpense expense, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String url = r2StorageService.uploadFinanceAttachment(file, "company-expenses", expense.getId());
            CompanyExpenseAttachment att = new CompanyExpenseAttachment();
            att.setCompanyExpense(expense);
            att.setUrl(url);
            att.setCreatedAt(LocalDateTime.now().toString());
            att.setUpdatedAt(LocalDateTime.now().toString());
            attachmentRepository.save(att);
        }
    }

    private String normalizeNullBlank(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

