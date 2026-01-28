package com.rh360.rh360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rh360.rh360.dto.CompanyExpenseRequest;
import com.rh360.rh360.dto.CompanyExpenseResponse;
import com.rh360.rh360.entity.CompanyExpense;
import com.rh360.rh360.repository.CompanyExpenseAttachmentRepository;
import com.rh360.rh360.repository.CompanyExpenseRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - CompanyExpenseService")
class CompanyExpenseServiceTest {

    @Mock
    private CompanyExpenseRepository repository;

    @Mock
    private CompanyExpenseAttachmentRepository attachmentRepository;

    @Mock
    private R2StorageService r2StorageService;

    @InjectMocks
    private CompanyExpenseService service;

    @Test
    @DisplayName("Deve criar gasto diverso com sucesso")
    void deveCriarComSucesso() {
        CompanyExpenseRequest req = new CompanyExpenseRequest();
        req.setTitle("Compra");
        req.setType("material");
        req.setDate(LocalDate.of(2026, 2, 1));
        req.setAmount(new BigDecimal("10.00"));

        when(repository.save(any(CompanyExpense.class))).thenAnswer(invocation -> {
            CompanyExpense e = invocation.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });
        when(attachmentRepository.findByCompanyExpense_IdAndDeletedAtIsNull(any(UUID.class))).thenReturn(List.of());

        CompanyExpenseResponse resp = service.create(req);
        assertNotNull(resp.getId());
        assertEquals("Compra", resp.getTitle());
    }

    @Test
    @DisplayName("Deve listar gastos diversos")
    void deveListar() {
        CompanyExpense e = new CompanyExpense();
        e.setId(UUID.randomUUID());
        e.setTitle("Teste");
        e.setType("outros");
        e.setDate(LocalDate.of(2026, 2, 1));
        when(repository.findByFilters(null, null, null)).thenReturn(List.of(e));
        when(attachmentRepository.findByCompanyExpense_IdAndDeletedAtIsNull(any(UUID.class))).thenReturn(List.of());

        List<CompanyExpenseResponse> list = service.list(null, null, null);
        assertEquals(1, list.size());
        assertEquals("Teste", list.get(0).getTitle());
    }

    @Test
    @DisplayName("Deve fazer soft delete")
    void deveDeletarSoft() {
        UUID id = UUID.randomUUID();
        CompanyExpense e = new CompanyExpense();
        e.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(e));
        when(repository.save(any(CompanyExpense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.delete(id);
        assertNotNull(e.getDeletedAt());
        verify(repository, times(1)).save(e);
    }
}

