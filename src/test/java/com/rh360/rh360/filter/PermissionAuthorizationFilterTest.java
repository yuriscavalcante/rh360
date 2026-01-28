package com.rh360.rh360.filter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rh360.rh360.util.SecurityUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - PermissionAuthorizationFilter")
class PermissionAuthorizationFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    @DisplayName("Deve permitir acesso a path excluído sem checar permissão")
    void devePermitirAcessoAPathExcluido() throws Exception {
        PermissionAuthorizationFilter filter = new PermissionAuthorizationFilter();
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("Deve bloquear com 403 quando rota não tem permissão configurada")
    void deveBloquearQuandoNaoConfigurado() throws Exception {
        PermissionAuthorizationFilter filter = new PermissionAuthorizationFilter();
        when(request.getRequestURI()).thenReturn("/api/unknown");
        when(request.getMethod()).thenReturn("GET");

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        filter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(filterChain, never()).doFilter(any(), any());
        assertTrue(sw.toString().contains("Permissão não configurada"));
    }

    @Test
    @DisplayName("Deve bloquear com 403 quando não possui permissão necessária")
    void deveBloquearQuandoSemPermissao() throws Exception {
        PermissionAuthorizationFilter filter = new PermissionAuthorizationFilter();
        when(request.getRequestURI()).thenReturn("/api/tasks");
        when(request.getMethod()).thenReturn("GET");

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.hasPermission(request, "VIEW_TASKS")).thenReturn(false);

            filter.doFilterInternal(request, response, filterChain);

            verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
            verify(filterChain, never()).doFilter(any(), any());
            assertTrue(sw.toString().contains("VIEW_TASKS"));
        }
    }

    @Test
    @DisplayName("Deve permitir quando possui permissão necessária")
    void devePermitirQuandoComPermissao() throws Exception {
        PermissionAuthorizationFilter filter = new PermissionAuthorizationFilter();
        when(request.getRequestURI()).thenReturn("/api/tasks");
        when(request.getMethod()).thenReturn("GET");

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.hasPermission(request, "VIEW_TASKS")).thenReturn(true);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain, times(1)).doFilter(request, response);
            verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}

