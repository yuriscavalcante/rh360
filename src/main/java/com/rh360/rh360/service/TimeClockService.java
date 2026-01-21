package com.rh360.rh360.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rh360.rh360.dto.TimeClockResponse;
import com.rh360.rh360.entity.TimeClock;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.repository.TimeClockRepository;
import com.rh360.rh360.service.CompreFaceService.FaceVerifyResponse;

@Service
public class TimeClockService {

    private static final Logger logger = LoggerFactory.getLogger(TimeClockService.class);
    
    private final TimeClockRepository repository;
    private final UsersService usersService;
    private final CompreFaceService compreFaceService;
    
    // Threshold mínimo de confiança para validar a face (0.7 = 70%)
    private static final double MIN_CONFIDENCE_THRESHOLD = 0.85;

    public TimeClockService(TimeClockRepository repository, UsersService usersService, 
                           CompreFaceService compreFaceService) {
        this.repository = repository;
        this.usersService = usersService;
        this.compreFaceService = compreFaceService;
    }

    /**
     * Registra um ponto para um usuário após validar sua face
     * 
     * @param userId ID do usuário
     * @param photo Foto para validação facial
     * @param message Mensagem opcional do registro de ponto
     * @return TimeClockResponse com o registro de ponto ou null se a validação falhar
     */
    public TimeClockResponse clockIn(UUID userId, MultipartFile photo, String message) {
        // Verificar se o usuário existe
        User user = usersService.findById(userId);
        if (user == null) {
            logger.warn("Tentativa de bater ponto para usuário inexistente: {}", userId);
            throw new RuntimeException("Usuário não encontrado");
        }

        // Verificar se a foto foi fornecida
        if (photo == null || photo.isEmpty()) {
            logger.warn("Tentativa de bater ponto sem foto para usuário: {}", userId);
            throw new RuntimeException("Foto é obrigatória para bater ponto");
        }

        // Validar a face usando CompreFace
        logger.info("Validando face do usuário {} para bater ponto", userId);
        FaceVerifyResponse verifyResponse = compreFaceService.verifyFace(userId, photo);

        if (verifyResponse == null) {
            logger.error("Erro ao verificar face do usuário {} no CompreFace", userId);
            throw new RuntimeException("Erro ao validar face no sistema de reconhecimento facial");
        }

        // Verificar se a face foi validada e se a confiança é suficiente
        if (!verifyResponse.isVerified() || verifyResponse.getConfidence() < MIN_CONFIDENCE_THRESHOLD) {
            logger.warn("Face do usuário {} não foi validada. Verified: {}, Confidence: {}", 
                userId, verifyResponse.isVerified(), verifyResponse.getConfidence());
            throw new RuntimeException(
                String.format("Face não validada. %s Confiança: %.2f%% (mínimo: %.2f%%)", 
                    verifyResponse.getMessage(), 
                    verifyResponse.getConfidence() * 100,
                    MIN_CONFIDENCE_THRESHOLD * 100)
            );
        }

        // Criar registro de ponto
        TimeClock timeClock = new TimeClock();
        timeClock.setUser(user);
        timeClock.setTimestamp(LocalDateTime.now());
        timeClock.setMessage(message);
        timeClock.setCreatedAt(LocalDateTime.now().toString());
        timeClock.setUpdatedAt(LocalDateTime.now().toString());

        // Salvar no banco
        TimeClock savedTimeClock = repository.save(timeClock);
        logger.info("✓ Ponto registrado com sucesso para usuário {} em {}", 
            userId, savedTimeClock.getTimestamp());

        // Criar resposta
        TimeClockResponse response = new TimeClockResponse(savedTimeClock);
        // Se não houver mensagem customizada, usar mensagem padrão
        if (response.getMessage() == null || response.getMessage().isEmpty()) {
            response.setMessage("Ponto registrado com sucesso");
        }
        response.setConfidence(verifyResponse.getConfidence());

        return response;
    }

    /**
     * Busca todos os registros de ponto de um usuário ordenados por timestamp (mais recentes primeiro)
     * 
     * @param userId ID do usuário
     * @return Lista de TimeClockResponse com os registros de ponto
     */
    public List<TimeClockResponse> findByUserId(UUID userId) {
        logger.info("Buscando pontos do usuário {}", userId);
        
        // Verificar se o usuário existe
        User user = usersService.findById(userId);
        if (user == null) {
            logger.warn("Tentativa de buscar pontos de usuário inexistente: {}", userId);
            throw new RuntimeException("Usuário não encontrado");
        }

        List<TimeClock> timeClocks = repository.findByUser_IdOrderByTimestampDesc(userId);
        logger.info("Encontrados {} registros de ponto para o usuário {}", timeClocks.size(), userId);

        return timeClocks.stream()
            .map(TimeClockResponse::new)
            .collect(Collectors.toList());
    }
}
