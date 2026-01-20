package com.rh360.rh360.service;

import com.rh360.rh360.entity.Token;
import com.rh360.rh360.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.qrcode.expiration:900000}")
    private Long qrCodeExpiration; // Padrão: 15 minutos (900000 ms)

    @Autowired
    private TokenRepository tokenRepository;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UUID userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("email", email);
        claims.put("role", role);
        
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expirationDate = new Date(System.currentTimeMillis() + expiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Token saveToken(String tokenString, UUID userId) {
        Date expirationDate = extractExpiration(tokenString);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(expirationDate.getTime()), 
                ZoneId.systemDefault()
        );

        Token token = new Token();
        token.setToken(tokenString);
        token.setUserId(userId);
        token.setActive(true);
        token.setExpiresAt(expiresAt);

        return tokenRepository.save(token);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        String userIdString = claims.get("userId", String.class);
        return UUID.fromString(userIdString);
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token) {
        try {
            // Primeiro valida se o token JWT está válido (não expirado)
            if (isTokenExpired(token)) {
                return false;
            }

            // Depois valida se o token está ativo no banco de dados
            return tokenRepository.findByTokenAndActiveTrue(token).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    public void deactivateToken(String token) {
        tokenRepository.findByToken(token).ifPresent(tokenEntity -> {
            tokenEntity.setActive(false);
            tokenRepository.save(tokenEntity);
        });
    }

    public void deactivateAllUserTokens(UUID userId) {
        java.util.List<Token> activeTokens = tokenRepository.findByUserIdAndActiveTrue(userId);
        activeTokens.forEach(token -> {
            token.setActive(false);
            tokenRepository.save(token);
        });
    }

    public Optional<Token> findActiveToken(String token) {
        return tokenRepository.findByTokenAndActiveTrue(token);
    }

    /**
     * Gera um token temporário para QR code com expiração reduzida
     * Este token é usado apenas para autenticação via QR code no mobile
     * 
     * @param userId ID do usuário
     * @param email Email do usuário
     * @param role Role do usuário
     * @return Token JWT temporário
     */
    public String generateQrCodeToken(UUID userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("email", email);
        claims.put("role", role);
        claims.put("type", "qrcode"); // Marca o token como tipo QR code
        
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expirationDate = new Date(System.currentTimeMillis() + qrCodeExpiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Salva um token de QR code no banco de dados
     * 
     * @param tokenString Token JWT
     * @param userId ID do usuário
     * @return Token salvo
     */
    public Token saveQrCodeToken(String tokenString, UUID userId) {
        return saveToken(tokenString, userId);
    }

    /**
     * Retorna o tempo de expiração dos tokens de QR code em milissegundos
     * 
     * @return Tempo de expiração em milissegundos
     */
    public Long getQrCodeExpiration() {
        return qrCodeExpiration;
    }
}
