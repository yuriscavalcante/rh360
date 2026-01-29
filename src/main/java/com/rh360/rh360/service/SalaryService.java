package com.rh360.rh360.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rh360.rh360.dto.SalaryRequest;
import com.rh360.rh360.dto.SalaryResponse;
import com.rh360.rh360.entity.Salary;
import com.rh360.rh360.entity.SalaryAttachment;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.realtime.RealTimeEvent;
import com.rh360.rh360.realtime.RealTimeTopic;
import com.rh360.rh360.realtime.NoOpRealTimePublisher;
import com.rh360.rh360.realtime.RealTimePublisher;
import com.rh360.rh360.repository.SalaryAttachmentRepository;
import com.rh360.rh360.repository.SalaryRepository;

@Service
public class SalaryService {

    private final SalaryRepository repository;
    private final SalaryAttachmentRepository attachmentRepository;
    private final UsersService usersService;
    private final R2StorageService r2StorageService;
    private final RealTimePublisher realTimePublisher;

    @Autowired
    public SalaryService(
            SalaryRepository repository,
            SalaryAttachmentRepository attachmentRepository,
            UsersService usersService,
            R2StorageService r2StorageService,
            RealTimePublisher realTimePublisher) {
        this.repository = repository;
        this.attachmentRepository = attachmentRepository;
        this.usersService = usersService;
        this.r2StorageService = r2StorageService;
        this.realTimePublisher = realTimePublisher != null ? realTimePublisher : NoOpRealTimePublisher.INSTANCE;
    }

    public SalaryService(
            SalaryRepository repository,
            SalaryAttachmentRepository attachmentRepository,
            UsersService usersService,
            R2StorageService r2StorageService) {
        this(repository, attachmentRepository, usersService, r2StorageService, NoOpRealTimePublisher.INSTANCE);
    }

    public SalaryResponse create(UUID userId, SalaryRequest request) {
        return create(userId, request, null);
    }

    public SalaryResponse create(UUID userId, SalaryRequest request, List<MultipartFile> files) {
        validateRequired(request);
        User user = usersService.findById(userId);
        if (user == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        Salary salary = new Salary();
        salary.setUser(user);
        applyRequest(salary, request);
        salary.setCreatedAt(LocalDateTime.now().toString());
        salary.setUpdatedAt(LocalDateTime.now().toString());
        if (salary.getStatus() == null || salary.getStatus().isBlank()) {
            salary.setStatus("pending");
        }

        Salary saved = repository.save(salary);
        saveAttachments(saved, files);
        List<SalaryAttachment> attachments = attachmentRepository.findBySalary_IdAndDeletedAtIsNull(saved.getId());
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.SALARIES, "refresh", userId.toString()));
        return new SalaryResponse(saved, attachments);
    }

    public SalaryResponse update(UUID salaryId, UUID userId, SalaryRequest request) {
        return update(salaryId, userId, request, null);
    }

    public SalaryResponse update(UUID salaryId, UUID userId, SalaryRequest request, List<MultipartFile> files) {
        Salary existing = repository.findById(salaryId).orElse(null);
        if (existing == null || existing.getDeletedAt() != null) {
            throw new RuntimeException("Salário não encontrado");
        }
        if (existing.getUser() == null || !existing.getUser().getId().equals(userId)) {
            throw new RuntimeException("Você não tem permissão para atualizar este salário");
        }

        applyRequest(existing, request);
        existing.setUpdatedAt(LocalDateTime.now().toString());
        Salary saved = repository.save(existing);
        saveAttachments(saved, files);
        List<SalaryAttachment> attachments = attachmentRepository.findBySalary_IdAndDeletedAtIsNull(saved.getId());
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.SALARIES, "refresh", userId.toString()));
        return new SalaryResponse(saved, attachments);
    }

    public void delete(UUID salaryId, UUID userId) {
        Salary existing = repository.findById(salaryId).orElse(null);
        if (existing == null || existing.getDeletedAt() != null) {
            throw new RuntimeException("Salário não encontrado");
        }
        if (existing.getUser() == null || !existing.getUser().getId().equals(userId)) {
            throw new RuntimeException("Você não tem permissão para deletar este salário");
        }
        existing.setDeletedAt(LocalDateTime.now().toString());
        existing.setUpdatedAt(LocalDateTime.now().toString());
        repository.save(existing);
        realTimePublisher.publish(new RealTimeEvent(RealTimeTopic.SALARIES, "refresh", userId.toString()));
    }

    public SalaryResponse findById(UUID salaryId, UUID userId) {
        Salary existing = repository.findById(salaryId).orElse(null);
        if (existing == null || existing.getDeletedAt() != null) {
            throw new RuntimeException("Salário não encontrado");
        }
        if (existing.getUser() == null || !existing.getUser().getId().equals(userId)) {
            throw new RuntimeException("Você não tem permissão para visualizar este salário");
        }
        List<SalaryAttachment> attachments = attachmentRepository.findBySalary_IdAndDeletedAtIsNull(existing.getId());
        return new SalaryResponse(existing, attachments);
    }

    public List<SalaryResponse> list(UUID userId, String referenceMonth, LocalDate fromPaidAt, LocalDate toPaidAt) {
        List<Salary> salaries;
        if (referenceMonth != null && !referenceMonth.isBlank()) {
            salaries = repository.findByUser_IdAndReferenceMonthAndDeletedAtIsNull(userId, referenceMonth);
        } else if (fromPaidAt != null || toPaidAt != null) {
            salaries = repository.findByUserIdAndPaidAtRange(userId, fromPaidAt, toPaidAt);
        } else {
            salaries = repository.findByUser_IdAndDeletedAtIsNullOrderByReferenceMonthDesc(userId);
        }

        return salaries.stream()
                .map(s -> new SalaryResponse(s, attachmentRepository.findBySalary_IdAndDeletedAtIsNull(s.getId())))
                .collect(Collectors.toList());
    }

    public BigDecimal getIncomeForMonth(UUID userId, String referenceMonth) {
        List<Salary> salaries = repository.findByUser_IdAndReferenceMonthAndDeletedAtIsNull(userId, referenceMonth);
        BigDecimal total = BigDecimal.ZERO;
        for (Salary s : salaries) {
            BigDecimal value = s.getNetAmount() != null ? s.getNetAmount() : s.getGrossAmount();
            if (value != null) {
                total = total.add(value);
            }
        }
        return total;
    }

    private void validateRequired(SalaryRequest request) {
        if (request.getReferenceMonth() == null || request.getReferenceMonth().isBlank()) {
            throw new RuntimeException("referenceMonth é obrigatório");
        }
        if (request.getGrossAmount() == null) {
            throw new RuntimeException("grossAmount é obrigatório");
        }
    }

    private void applyRequest(Salary salary, SalaryRequest request) {
        if (request.getReferenceMonth() != null) {
            salary.setReferenceMonth(request.getReferenceMonth());
        }
        if (request.getGrossAmount() != null) {
            salary.setGrossAmount(request.getGrossAmount());
        }
        if (request.getNetAmount() != null) {
            salary.setNetAmount(request.getNetAmount());
        }
        if (request.getDiscounts() != null) {
            salary.setDiscounts(request.getDiscounts());
        }
        if (request.getBonuses() != null) {
            salary.setBonuses(request.getBonuses());
        }
        if (request.getPaidAt() != null) {
            salary.setPaidAt(request.getPaidAt());
        }
        if (request.getNotes() != null) {
            salary.setNotes(request.getNotes());
        }
        if (request.getStatus() != null) {
            salary.setStatus(request.getStatus());
        }
    }

    private void saveAttachments(Salary salary, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String url = r2StorageService.uploadFinanceAttachment(file, "salaries", salary.getId());
            SalaryAttachment att = new SalaryAttachment();
            att.setSalary(salary);
            att.setUrl(url);
            att.setCreatedAt(LocalDateTime.now().toString());
            att.setUpdatedAt(LocalDateTime.now().toString());
            attachmentRepository.save(att);
        }
    }
}

