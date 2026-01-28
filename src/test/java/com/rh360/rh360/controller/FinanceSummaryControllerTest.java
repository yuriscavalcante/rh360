package com.rh360.rh360.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.rh360.rh360.dto.FinanceSummaryResponse;
import com.rh360.rh360.service.FinanceSummaryService;
import com.rh360.rh360.util.SecurityUtil;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - FinanceSummaryController")
class FinanceSummaryControllerTest {

    @Mock
    private FinanceSummaryService financeSummaryService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private FinanceSummaryController controller;

    @Test
    @DisplayName("Deve retornar 401 quando não autenticado")
    void deveRetornar401QuandoNaoAutenticado() {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getUserId(request)).thenReturn(null);
            ResponseEntity<?> resp = controller.summaryMe("2026-01", request);
            assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        }
    }

    @Test
    @DisplayName("Deve retornar resumo quando autenticado")
    void deveRetornarResumoQuandoAutenticado() {
        UUID userId = UUID.randomUUID();
        FinanceSummaryResponse summary = new FinanceSummaryResponse();
        summary.setReferenceMonth("2026-01");
        summary.setTotalIncome(new BigDecimal("100.00"));
        summary.setTotalExpenses(new BigDecimal("40.00"));
        summary.setBalance(new BigDecimal("60.00"));

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getUserId(request)).thenReturn(userId);
            when(financeSummaryService.summaryForMonth(userId, "2026-01")).thenReturn(summary);

            ResponseEntity<?> resp = controller.summaryMe("2026-01", request);
            assertEquals(HttpStatus.OK, resp.getStatusCode());
            assertNotNull(resp.getBody());
        }
    }
}

