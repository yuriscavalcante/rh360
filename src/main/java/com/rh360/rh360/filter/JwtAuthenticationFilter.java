package com.rh360.rh360.filter;

import com.rh360.rh360.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
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

        // Permitir requisições sem autenticação para paths específicos
        if (isExcludedPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token não fornecido ou formato inválido\"}");
            response.setContentType("application/json");
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer "

        try {
            if (!tokenService.validateToken(token)) {
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Erro ao validar token: " + e.getMessage() + "\"}");
            response.setContentType("application/json");
        }
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }
}
