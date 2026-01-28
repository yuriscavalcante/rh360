package com.rh360.rh360.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.AntPathMatcher;

import com.rh360.rh360.util.SecurityUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PermissionAuthorizationFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/login",
            "/error",
            "/actuator",
            "/swagger-ui",
            "/swagger-ui.html",
            "/api-docs",
            "/v3/api-docs",
            // mobile via QR token (não usa JWT)
            "/api/timeclock/mobile"
    );

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // só aplica em /api/*
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String required = requiredPermission(request);
        if (required == null) {
            deny(response, "Permissão não configurada para esta rota");
            return;
        }

        if (!SecurityUtil.hasPermission(request, required)) {
            deny(response, "Acesso negado: permissão necessária '" + required + "'");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private void deny(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message.replace("\"", "\\\"") + "\"}");
    }

    private String requiredPermission(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        // Auth
        if (matches(method, "/api/auth/logout", HttpMethod.POST) && matcher.match("/api/auth/logout", path)) {
            return "VIEW_PROFILE";
        }

        // Permission management
        if (matcher.match("/api/permissions/**", path) || matcher.match("/api/permission-templates/**", path)) {
            return "CREATE_PERMISSIONS";
        }

        // Users
        if (matcher.match("/api/users/me", path) && matches(method, null, HttpMethod.GET)) {
            return "VIEW_PROFILE";
        }
        if (matcher.match("/api/users/**", path)) {
            if (matches(method, null, HttpMethod.POST)) return "CREATE_USER";
            if (matches(method, null, HttpMethod.PUT)) return "EDIT_USER";
            if (matches(method, null, HttpMethod.DELETE)) return "EDIT_USER";
            if (matches(method, null, HttpMethod.GET)) return "EDIT_USER"; // listar/pegar por id
        }

        // Teams
        if (matcher.match("/api/teams/**", path)) {
            if (matches(method, null, HttpMethod.GET)) return "VIEW_TEAMS";
            if (matches(method, null, HttpMethod.POST)) return "CREATE_TEAM";
            if (matches(method, null, HttpMethod.PUT)) return "CREATE_TEAM";
            if (matches(method, null, HttpMethod.DELETE)) return "CREATE_TEAM";
        }

        // Tasks
        if (matcher.match("/api/tasks/**", path)) {
            return "VIEW_TASKS";
        }

        // Timeclock (ponto)
        if (matcher.match("/api/timeclock/qr-code", path) && matches(method, null, HttpMethod.GET)) {
            return "ATTENDANCE_MOBILE";
        }
        if (matcher.match("/api/timeclock/**", path)) {
            return "ATTENDANCE";
        }

        // Faces (validação facial)
        if (matcher.match("/api/faces/**", path)) {
            return "ATTENDANCE";
        }

        // Financeiro (mesma ideia: pode granular depois)
        if (matcher.match("/api/finance/**", path)) {
            return "VIEW_DASHBOARD";
        }

        // Hello (roteiro básico)
        if (matcher.match("/api/hello", path) && matches(method, null, HttpMethod.GET)) {
            return "VIEW_DASHBOARD";
        }

        return null;
    }

    private boolean matches(String method, String exactPath, HttpMethod expected) {
        if (method == null) return false;
        return expected.matches(method);
    }
}

