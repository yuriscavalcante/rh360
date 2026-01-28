package com.rh360.rh360.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rh360.rh360.entity.Permission;
import com.rh360.rh360.repository.PermissionRepository;

@Component
public class SecurityUtil {

    private static PermissionRepository permissionRepository;

    public SecurityUtil(PermissionRepository permissionRepository) {
        SecurityUtil.permissionRepository = permissionRepository;
    }

    public static UUID getUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            return null;
        }
        try {
            return (UUID) userId;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public static String getEmail(HttpServletRequest request) {
        Object email = request.getAttribute("email");
        return email != null ? (String) email : null;
    }

    public static String getRole(HttpServletRequest request) {
        Object role = request.getAttribute("role");
        return role != null ? (String) role : null;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        String role = getRole(request);
        return role != null && role.equalsIgnoreCase("admin");
    }

    public static boolean hasPermission(HttpServletRequest request, String function) {
        if (function == null || function.isBlank()) {
            return false;
        }

        // bypass opcional para admin (mantém compatibilidade)
        if (isAdmin(request)) {
            return true;
        }

        UUID userId = getUserId(request);
        if (userId == null) {
            return false;
        }

        if (permissionRepository == null) {
            // Em caso de uso fora do contexto Spring (ex.: testes isolados), falha fechado.
            return false;
        }

        Optional<Permission> permission = permissionRepository.findByUserIdAndFunction(userId, function);
        if (permission.isEmpty()) {
            return false;
        }

        Permission p = permission.get();
        if (p.getDeletedAt() != null && !p.getDeletedAt().isBlank()) {
            return false;
        }

        return Boolean.TRUE.equals(p.getIsPermitted());
    }
}
