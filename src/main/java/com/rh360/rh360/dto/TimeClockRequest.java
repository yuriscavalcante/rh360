package com.rh360.rh360.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Requisição para bater ponto")
public class TimeClockRequest {
    
    @Schema(description = "ID do usuário que está batendo o ponto", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;
}
