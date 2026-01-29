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

import com.rh360.rh360.dto.ExpenseRequest;
import com.rh360.rh360.dto.ExpenseResponse;
import com.rh360.rh360.entity.Expense;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.repository.ExpenseAttachmentRepository;
import com.rh360.rh360.repository.ExpenseRepository;
import com.rh360.rh360.realtime.RealTimePublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ExpenseService")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseAttachmentRepository expenseAttachmentRepository;

    @Mock
    private UsersService usersService;

    @Mock
    private R2StorageService r2StorageService;

    @Mock
    private RealTimePublisher realTimePublisher;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    @DisplayName("Deve criar despesa com sucesso")
    void deveCriarDespesaComSucesso() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        ExpenseRequest req = new ExpenseRequest();
        req.setDate(LocalDate.of(2026, 1, 10));
        req.setAmount(new BigDecimal("120.50"));
        req.setDescription("Almoço");

        when(usersService.findById(userId)).thenReturn(user);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense e = invocation.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });
        when(expenseAttachmentRepository.findByExpense_IdAndDeletedAtIsNull(any(UUID.class)))
            .thenReturn(List.of());

        ExpenseResponse resp = expenseService.create(userId, req);

        assertNotNull(resp);
        assertEquals(new BigDecimal("120.50"), resp.getAmount());
        assertEquals("Almoço", resp.getDescription());
        verify(usersService, times(1)).findById(userId);
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(expenseAttachmentRepository, atLeastOnce()).findByExpense_IdAndDeletedAtIsNull(any(UUID.class));
    }

    @Test
    @DisplayName("Deve listar despesas do usuário")
    void deveListarDespesas() {
        UUID userId = UUID.randomUUID();
        User u = new User();
        u.setId(userId);
        Expense e = new Expense();
        e.setId(UUID.randomUUID());
        e.setUser(u);
        e.setDate(LocalDate.of(2026, 1, 10));
        e.setAmount(new BigDecimal("10.00"));
        e.setDescription("Teste");

        when(expenseRepository.findByUser_IdAndDeletedAtIsNullOrderByDateDesc(userId))
            .thenReturn(List.of(e));
        when(expenseAttachmentRepository.findByExpense_IdAndDeletedAtIsNull(any(UUID.class)))
            .thenReturn(List.of());

        List<ExpenseResponse> list = expenseService.list(userId, null, null, null);
        assertEquals(1, list.size());
        assertEquals("Teste", list.get(0).getDescription());
    }

    @Test
    @DisplayName("Deve fazer soft delete de despesa")
    void deveDeletarDespesaSoftDelete() {
        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        User u = new User();
        u.setId(userId);
        Expense e = new Expense();
        e.setId(expenseId);
        e.setUser(u);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(e));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        expenseService.delete(expenseId, userId);

        assertNotNull(e.getDeletedAt());
        verify(expenseRepository, times(1)).save(e);
    }
}

