package com.rh360.rh360.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rh360.rh360.dto.ExpenseRequest;
import com.rh360.rh360.dto.ExpenseResponse;
import com.rh360.rh360.entity.Expense;
import com.rh360.rh360.entity.ExpenseAttachment;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.repository.ExpenseAttachmentRepository;
import com.rh360.rh360.repository.ExpenseRepository;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;
    private final ExpenseAttachmentRepository attachmentRepository;
    private final UsersService usersService;
    private final R2StorageService r2StorageService;

    public ExpenseService(
            ExpenseRepository repository,
            ExpenseAttachmentRepository attachmentRepository,
            UsersService usersService,
            R2StorageService r2StorageService) {
        this.repository = repository;
        this.attachmentRepository = attachmentRepository;
        this.usersService = usersService;
        this.r2StorageService = r2StorageService;
    }

    public ExpenseResponse create(UUID userId, ExpenseRequest request) {
        return create(userId, request, null);
    }

    public ExpenseResponse create(UUID userId, ExpenseRequest request, List<MultipartFile> files) {
        validateRequired(request);
        User user = usersService.findById(userId);
        if (user == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        Expense expense = new Expense();
        expense.setUser(user);
        applyRequest(expense, request);
        expense.setCreatedAt(LocalDateTime.now().toString());
        expense.setUpdatedAt(LocalDateTime.now().toString());
        if (expense.getStatus() == null || expense.getStatus().isBlank()) {
            expense.setStatus("paid");
        }

        Expense saved = repository.save(expense);
        saveAttachments(saved, files);
        List<ExpenseAttachment> attachments = attachmentRepository.findByExpense_IdAndDeletedAtIsNull(saved.getId());
        return new ExpenseResponse(saved, attachments);
    }

    public ExpenseResponse update(UUID expenseId, UUID userId, ExpenseRequest request) {
        return update(expenseId, userId, request, null);
    }

    public ExpenseResponse update(UUID expenseId, UUID userId, ExpenseRequest request, List<MultipartFile> files) {
        Expense existing = repository.findById(expenseId).orElse(null);
        if (existing == null || existing.getDeletedAt() != null) {
            throw new RuntimeException("Despesa não encontrada");
        }
        if (existing.getUser() == null || !existing.getUser().getId().equals(userId)) {
            throw new RuntimeException("Você não tem permissão para atualizar esta despesa");
        }

        applyRequest(existing, request);
        existing.setUpdatedAt(LocalDateTime.now().toString());
        Expense saved = repository.save(existing);
        saveAttachments(saved, files);
        List<ExpenseAttachment> attachments = attachmentRepository.findByExpense_IdAndDeletedAtIsNull(saved.getId());
        return new ExpenseResponse(saved, attachments);
    }

    public void delete(UUID expenseId, UUID userId) {
        Expense existing = repository.findById(expenseId).orElse(null);
        if (existing == null || existing.getDeletedAt() != null) {
            throw new RuntimeException("Despesa não encontrada");
        }
        if (existing.getUser() == null || !existing.getUser().getId().equals(userId)) {
            throw new RuntimeException("Você não tem permissão para deletar esta despesa");
        }
        existing.setDeletedAt(LocalDateTime.now().toString());
        existing.setUpdatedAt(LocalDateTime.now().toString());
        repository.save(existing);
    }

    public ExpenseResponse findById(UUID expenseId, UUID userId) {
        Expense existing = repository.findById(expenseId).orElse(null);
        if (existing == null || existing.getDeletedAt() != null) {
            throw new RuntimeException("Despesa não encontrada");
        }
        if (existing.getUser() == null || !existing.getUser().getId().equals(userId)) {
            throw new RuntimeException("Você não tem permissão para visualizar esta despesa");
        }
        List<ExpenseAttachment> attachments = attachmentRepository.findByExpense_IdAndDeletedAtIsNull(existing.getId());
        return new ExpenseResponse(existing, attachments);
    }

    public List<ExpenseResponse> list(UUID userId, LocalDate from, LocalDate to, String category) {
        List<Expense> expenses;
        if (from != null || to != null || (category != null && !category.isBlank())) {
            expenses = repository.findByUserIdAndFilters(userId, from, to, normalizeNullBlank(category));
        } else {
            expenses = repository.findByUser_IdAndDeletedAtIsNullOrderByDateDesc(userId);
        }
        return expenses.stream()
                .map(e -> new ExpenseResponse(e, attachmentRepository.findByExpense_IdAndDeletedAtIsNull(e.getId())))
                .collect(Collectors.toList());
    }

    public BigDecimal getExpensesForMonth(UUID userId, LocalDate from, LocalDate to) {
        List<Expense> expenses = repository.findByUserIdAndFilters(userId, from, to, null);
        BigDecimal total = BigDecimal.ZERO;
        for (Expense e : expenses) {
            if (e.getAmount() != null) {
                total = total.add(e.getAmount());
            }
        }
        return total;
    }

    private void validateRequired(ExpenseRequest request) {
        if (request.getDate() == null) {
            throw new RuntimeException("date é obrigatório");
        }
        if (request.getAmount() == null) {
            throw new RuntimeException("amount é obrigatório");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new RuntimeException("description é obrigatório");
        }
    }

    private void applyRequest(Expense expense, ExpenseRequest request) {
        if (request.getDate() != null) {
            expense.setDate(request.getDate());
        }
        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            expense.setCategory(request.getCategory());
        }
        if (request.getPaymentMethod() != null) {
            expense.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getVendor() != null) {
            expense.setVendor(request.getVendor());
        }
        if (request.getStatus() != null) {
            expense.setStatus(request.getStatus());
        }
    }

    private String normalizeNullBlank(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void saveAttachments(Expense expense, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String url = r2StorageService.uploadFinanceAttachment(file, "expenses", expense.getId());
            ExpenseAttachment att = new ExpenseAttachment();
            att.setExpense(expense);
            att.setUrl(url);
            att.setCreatedAt(LocalDateTime.now().toString());
            att.setUpdatedAt(LocalDateTime.now().toString());
            attachmentRepository.save(att);
        }
    }
}

