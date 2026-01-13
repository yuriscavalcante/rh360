package com.rh360.rh360.filter;

import com.rh360.rh360.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/login",
            "/error",
            "/actuator",
            "/swagger-ui",
            "/swagger-ui.html",
            "/api-docs",
            "/v3/api-docs"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        // Permitir requisições OPTIONS (preflight CORS)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Permitir requisições sem autenticação para paths específicos
        // Verificar ANTES de validar token
        if (isExcludedPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Adicionar headers CORS antes de retornar erro
            addCorsHeaders(request, response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token não fornecido ou formato inválido\"}");
            response.setContentType("application/json");
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer "

        try {
            if (!tokenService.validateToken(token)) {
                // Adicionar headers CORS antes de retornar erro
                addCorsHeaders(request, response);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Token inválido, expirado ou inativo\"}");
                response.setContentType("application/json");
                return;
            }

            // Adicionar informações do usuário ao request para uso nos controllers
            Long userId = tokenService.extractUserId(token);
            String email = tokenService.extractEmail(token);
            String role = tokenService.extractRole(token);

            request.setAttribute("userId", userId);
            request.setAttribute("email", email);
            request.setAttribute("role", role);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // Adicionar headers CORS antes de retornar erro
            addCorsHeaders(request, response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Erro ao validar token: " + e.getMessage() + "\"}");
            response.setContentType("application/json");
        }
    }

    private void addCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        // Garantir que headers CORS estejam presentes mesmo em caso de erro
        String origin = request.getHeader("Origin");
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Expose-Headers", "Authorization, Content-Type");
    }

    private boolean isExcludedPath(String path) {
        // Normalizar o path (remover query string se houver)
        String normalizedPath = path.split("\\?")[0];
        
        // Verificar se o path corresponde exatamente ou começa com algum dos paths excluídos
        for (String excluded : EXCLUDED_PATHS) {
            if (normalizedPath.equals(excluded) || normalizedPath.startsWith(excluded)) {
                return true;
            }
        }
        return false;
    }
}
