package com.rh360.rh360.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Testes Unitários - DateUtil")
class DateUtilTest {

    @Test
    @DisplayName("Deve parsear data ISO yyyy-MM-dd")
    void deveParsearIso() {
        assertEquals(LocalDate.of(2026, 2, 1), DateUtil.parseFlexibleDate("2026-02-01"));
    }

    @Test
    @DisplayName("Deve parsear data BR dd/MM/yyyy")
    void deveParsearBr() {
        assertEquals(LocalDate.of(2026, 2, 1), DateUtil.parseFlexibleDate("01/02/2026"));
    }

    @Test
    @DisplayName("Deve falhar com mensagem clara para data inválida")
    void deveFalharParaInvalida() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> DateUtil.parseFlexibleDate("2026/02/01"));
        assertTrue(ex.getMessage().contains("Data inválida"));
    }
}

