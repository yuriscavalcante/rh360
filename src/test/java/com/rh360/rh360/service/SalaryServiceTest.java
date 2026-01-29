package com.rh360.rh360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rh360.rh360.dto.SalaryRequest;
import com.rh360.rh360.dto.SalaryResponse;
import com.rh360.rh360.entity.Salary;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.repository.SalaryAttachmentRepository;
import com.rh360.rh360.repository.SalaryRepository;
import com.rh360.rh360.realtime.RealTimePublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - SalaryService")
class SalaryServiceTest {

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private SalaryAttachmentRepository salaryAttachmentRepository;

    @Mock
    private UsersService usersService;

    @Mock
    private R2StorageService r2StorageService;

    @Mock
    private RealTimePublisher realTimePublisher;

    @InjectMocks
    private SalaryService salaryService;

    @Test
    @DisplayName("Deve criar salário com sucesso")
    void deveCriarSalarioComSucesso() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        SalaryRequest req = new SalaryRequest();
        req.setReferenceMonth("2026-01");
        req.setGrossAmount(new BigDecimal("5000.00"));

        when(usersService.findById(userId)).thenReturn(user);
        when(salaryRepository.save(any(Salary.class))).thenAnswer(invocation -> {
            Salary s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        when(salaryAttachmentRepository.findBySalary_IdAndDeletedAtIsNull(any(UUID.class)))
            .thenReturn(List.of());

        SalaryResponse resp = salaryService.create(userId, req);

        assertNotNull(resp);
        assertEquals("2026-01", resp.getReferenceMonth());
        assertEquals(new BigDecimal("5000.00"), resp.getGrossAmount());
        verify(usersService, times(1)).findById(userId);
        verify(salaryRepository, times(1)).save(any(Salary.class));
        verify(salaryAttachmentRepository, atLeastOnce()).findBySalary_IdAndDeletedAtIsNull(any(UUID.class));
    }

    @Test
    @DisplayName("Deve listar salários do usuário")
    void deveListarSalarios() {
        UUID userId = UUID.randomUUID();
        Salary s = new Salary();
        s.setId(UUID.randomUUID());
        User u = new User();
        u.setId(userId);
        s.setUser(u);
        s.setReferenceMonth("2026-01");
        s.setGrossAmount(new BigDecimal("100.00"));

        when(salaryRepository.findByUser_IdAndDeletedAtIsNullOrderByReferenceMonthDesc(userId))
            .thenReturn(List.of(s));
        when(salaryAttachmentRepository.findBySalary_IdAndDeletedAtIsNull(any(UUID.class)))
            .thenReturn(List.of());

        List<SalaryResponse> list = salaryService.list(userId, null, null, null);
        assertEquals(1, list.size());
        assertEquals("2026-01", list.get(0).getReferenceMonth());
    }

    @Test
    @DisplayName("Deve fazer soft delete de salário")
    void deveDeletarSalarioSoftDelete() {
        UUID userId = UUID.randomUUID();
        UUID salaryId = UUID.randomUUID();
        User u = new User();
        u.setId(userId);
        Salary s = new Salary();
        s.setId(salaryId);
        s.setUser(u);

        when(salaryRepository.findById(salaryId)).thenReturn(Optional.of(s));
        when(salaryRepository.save(any(Salary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        salaryService.delete(salaryId, userId);

        assertNotNull(s.getDeletedAt());
        verify(salaryRepository, times(1)).save(s);
    }
}

