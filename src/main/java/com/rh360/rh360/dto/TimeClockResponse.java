package com.rh360.rh360.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.rh360.rh360.entity.TimeClock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resposta de registro de ponto")
public class TimeClockResponse {
    
    @Schema(description = "ID do registro de ponto", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "ID do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;
    
    @Schema(description = "Nome do usuário", example = "João da Silva")
    private String userName;
    
    @Schema(description = "Data e hora do registro de ponto", example = "2024-01-15T08:30:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "Data de criação do registro", example = "2024-01-15T08:30:00")
    private String createdAt;
    
    @Schema(description = "Mensagem do registro de ponto", example = "Entrada no trabalho")
    private String message;
    
    @Schema(description = "Nível de confiança da validação facial", example = "0.95")
    private Double confidence;

    public TimeClockResponse(TimeClock timeClock) {
        this.id = timeClock.getId();
        this.userId = timeClock.getUser() != null ? timeClock.getUser().getId() : null;
        this.userName = timeClock.getUser() != null ? timeClock.getUser().getName() : null;
        this.timestamp = timeClock.getTimestamp();
        this.message = timeClock.getMessage();
        this.createdAt = timeClock.getCreatedAt();
    }
    
    public TimeClockResponse() {
    }
}
