package com.rh360.rh360.websocket;

import com.rh360.rh360.service.TokenService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;
import java.util.UUID;

/**
 * Autentica WebSocket STOMP no CONNECT via header Authorization: Bearer <token>
 * e define o Principal como userId (String) para permitir convertAndSendToUser(userId,...)
 */
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private final TokenService tokenService;

    public JwtStompChannelInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = firstNativeHeader(accessor, "Authorization");
            if (authHeader == null || authHeader.isBlank()) {
                authHeader = firstNativeHeader(accessor, "authorization");
            }

            String token = null;
            if (authHeader != null) {
                token = authHeader.trim();
                if (token.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
                    token = token.substring("Bearer ".length()).trim();
                }
            }

            if (token == null || token.isBlank() || !Boolean.TRUE.equals(tokenService.validateToken(token))) {
                // Sem Spring Security HTTP, mas aqui podemos bloquear o CONNECT
                throw new IllegalArgumentException("Token JWT ausente ou inválido no CONNECT");
            }

            UUID userId = tokenService.extractUserId(token);
            String userKey = userId.toString();

            accessor.setUser(new StompUserPrincipal(userKey));

            // deixa alguns dados acessíveis pra handlers se quiser
            accessor.getSessionAttributes().put("userId", userKey);
            accessor.getSessionAttributes().put("email", tokenService.extractEmail(token));
            accessor.getSessionAttributes().put("role", tokenService.extractRole(token));
        }

        return message;
    }

    private String firstNativeHeader(StompHeaderAccessor accessor, String name) {
        return accessor.getFirstNativeHeader(name);
    }

    private static final class StompUserPrincipal implements Principal {
        private final String name;

        private StompUserPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}

