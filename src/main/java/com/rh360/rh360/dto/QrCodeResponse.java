package com.rh360.rh360.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta com QR code para bater ponto via mobile")
public class QrCodeResponse {
    
    @Schema(description = "QR code em formato Base64 (PNG)", 
            example = "iVBORw0KGgoAAAANSUhEUgAA...")
    private String qrCodeBase64;
    
    @Schema(description = "URL que será aberta ao escanear o QR code", 
            example = "http://localhost:3000/timeclock/mobile?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String url;
    
    @Schema(description = "Token temporário do QR code (para referência)", 
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "Tempo de expiração do token em minutos", example = "15")
    private Long expiresInMinutes;
}
