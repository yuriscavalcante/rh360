package com.rh360.rh360.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DateUtil() {}

    public static LocalDate parseFlexibleDate(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;

        // Primeiro tenta ISO (yyyy-MM-dd)
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException ignored) {}

        // Depois tenta BR (dd/MM/yyyy)
        try {
            return LocalDate.parse(v, BR);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Data inválida. Use 'yyyy-MM-dd' ou 'dd/MM/yyyy'. Valor: " + value);
        }
    }
}

