package com.rh360.rh360.service;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class CompreFaceService {

    private static final Logger logger = LoggerFactory.getLogger(CompreFaceService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${compreface.api.url:http://localhost:8000}")
    private String comprefaceApiUrl;
    
    @Value("${compreface.api.key:}")
    private String comprefaceApiKey;
    
    @Value("${compreface.subject.endpoint:/api/v1/recognition/faces}")
    private String subjectEndpoint;
    
    @Value("${compreface.verify.endpoint:/api/v1/verification/verify}")
    private String verifyEndpoint;
    
    public CompreFaceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Adiciona uma face ao CompreFace associada a um usuário (subject)
     * 
     * @param userId ID do usuário que será usado como subject no CompreFace
     * @param imageFile Arquivo de imagem contendo o rosto
     * @return true se a face foi adicionada com sucesso, false caso contrário
     */
    public boolean addFace(UUID userId, MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            logger.warn("Arquivo de imagem vazio para o usuário {}. Não é possível adicionar face.", userId);
            return false;
        }
        
        if (comprefaceApiKey == null || comprefaceApiKey.isEmpty()) {
            logger.error("API Key do CompreFace não configurada. Configure a propriedade compreface.api.key");
            return false;
        }
        
        try {
            // Garantir que não há barras duplas na URL
            String baseUrl = comprefaceApiUrl.endsWith("/") 
                ? comprefaceApiUrl.substring(0, comprefaceApiUrl.length() - 1) 
                : comprefaceApiUrl;
            String endpoint = subjectEndpoint.startsWith("/") 
                ? subjectEndpoint 
                : "/" + subjectEndpoint;
            String url = baseUrl + endpoint;
            
            // Preparar o body multipart
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartFileResource(imageFile));
            body.add("subject", userId.toString());
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("x-api-key", comprefaceApiKey);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            logger.info("=== CompreFace - Adicionar Face (Multipart) ===");
            logger.info("URL completa: {}", url);
            logger.info("User ID (subject): {}", userId);
            logger.info("Arquivo: {} ({} bytes)", imageFile.getOriginalFilename(), imageFile.getSize());
            logger.info("API Key: {}...{}", comprefaceApiKey.substring(0, Math.min(8, comprefaceApiKey.length())), 
                comprefaceApiKey.substring(Math.max(0, comprefaceApiKey.length() - 4)));
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(url, requestEntity, Map.class);
            
            logger.info("Resposta do CompreFace - Status: {}", response.getStatusCode());
            if (response.getBody() != null) {
                logger.info("Resposta do CompreFace - Body: {}", response.getBody());
            }
            
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                logger.info("✓ Face do usuário {} adicionada com sucesso ao CompreFace", userId);
                return true;
            } else {
                logger.error("✗ Falha ao adicionar face do usuário {} ao CompreFace. Status: {}, Body: {}", 
                    userId, response.getStatusCode(), response.getBody());
                return false;
            }
            
        } catch (RestClientException e) {
            logger.error("✗ Erro ao adicionar face do usuário {} ao CompreFace", userId, e);
            logger.error("Detalhes do erro: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Causa: {}", e.getCause().getMessage());
            }
            return false;
        } catch (Exception e) {
            logger.error("✗ Erro inesperado ao adicionar face do usuário {} ao CompreFace: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Adiciona uma face ao CompreFace usando uma URL de imagem
     * 
     * @param userId ID do usuário que será usado como subject no CompreFace
     * @param imageUrl URL da imagem contendo o rosto
     * @return true se a face foi adicionada com sucesso, false caso contrário
     */
    public boolean addFaceFromUrl(UUID userId, String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            logger.warn("URL da imagem vazia para o usuário {}. Não é possível adicionar face.", userId);
            return false;
        }
        
        if (comprefaceApiKey == null || comprefaceApiKey.isEmpty()) {
            logger.error("API Key do CompreFace não configurada. Configure a propriedade compreface.api.key");
            return false;
        }
        
        try {
            // Garantir que não há barras duplas na URL
            String baseUrl = comprefaceApiUrl.endsWith("/") 
                ? comprefaceApiUrl.substring(0, comprefaceApiUrl.length() - 1) 
                : comprefaceApiUrl;
            String endpoint = subjectEndpoint.startsWith("/") 
                ? subjectEndpoint 
                : "/" + subjectEndpoint;
            String url = baseUrl + endpoint;
            
            // Preparar o body JSON
            Map<String, String> requestBody = Map.of(
                "url", imageUrl,
                "subject", userId.toString()
            );
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", comprefaceApiKey);
            
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            logger.info("=== CompreFace - Adicionar Face ===");
            logger.info("URL completa: {}", url);
            logger.info("User ID (subject): {}", userId);
            logger.info("Photo URL: {}", imageUrl);
            logger.info("API Key: {}...{}", comprefaceApiKey.substring(0, Math.min(8, comprefaceApiKey.length())), 
                comprefaceApiKey.substring(Math.max(0, comprefaceApiKey.length() - 4)));
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(url, requestEntity, Map.class);
            
            logger.info("Resposta do CompreFace - Status: {}", response.getStatusCode());
            if (response.getBody() != null) {
                logger.info("Resposta do CompreFace - Body: {}", response.getBody());
            }
            
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                logger.info("✓ Face do usuário {} adicionada com sucesso ao CompreFace via URL", userId);
                return true;
            } else {
                logger.error("✗ Falha ao adicionar face do usuário {} ao CompreFace via URL. Status: {}, Body: {}", 
                    userId, response.getStatusCode(), response.getBody());
                return false;
            }
            
        } catch (RestClientException e) {
            logger.error("✗ Erro ao adicionar face do usuário {} ao CompreFace via URL", userId, e);
            logger.error("Detalhes do erro: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Causa: {}", e.getCause().getMessage());
            }
            return false;
        } catch (Exception e) {
            logger.error("✗ Erro inesperado ao adicionar face do usuário {} ao CompreFace: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Verifica se uma face corresponde ao usuário cadastrado
     * 
     * @param userId ID do usuário para verificação
     * @param imageFile Arquivo de imagem contendo o rosto a verificar
     * @return FaceVerifyResponse com resultado da verificação, ou null em caso de erro
     */
    public FaceVerifyResponse verifyFace(UUID userId, MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            logger.warn("Arquivo de imagem vazio para verificação do usuário {}.", userId);
            return null;
        }
        
        if (comprefaceApiKey == null || comprefaceApiKey.isEmpty()) {
            logger.error("API Key do CompreFace não configurada. Configure a propriedade compreface.api.key");
            return null;
        }
        
        try {
            // Garantir que não há barras duplas na URL
            String baseUrl = comprefaceApiUrl.endsWith("/") 
                ? comprefaceApiUrl.substring(0, comprefaceApiUrl.length() - 1) 
                : comprefaceApiUrl;
            String endpoint = verifyEndpoint.startsWith("/") 
                ? verifyEndpoint 
                : "/" + verifyEndpoint;
            String url = baseUrl + endpoint;
            
            // Preparar o body multipart (Recognition Service não precisa do subject no body)
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartFileResource(imageFile));
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("x-api-key", comprefaceApiKey);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            logger.info("=== CompreFace - Verificar Face (Recognition) ===");
            logger.info("URL completa: {}", url);
            logger.info("User ID esperado (subject): {}", userId);
            logger.info("Arquivo: {} ({} bytes)", imageFile.getOriginalFilename(), imageFile.getSize());
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(url, requestEntity, Map.class);
            
            logger.info("Resposta do CompreFace - Status: {}", response.getStatusCode());
            if (response.getBody() != null) {
                logger.info("Resposta do CompreFace - Body: {}", response.getBody());
            }
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                
                logger.info("=== Processando resposta do CompreFace ===");
                logger.info("Resposta completa: {}", responseBody);
                
                FaceVerifyResponse verifyResponse = new FaceVerifyResponse();
                verifyResponse.setUser_id(userId.toString());
                
                // Processar resposta do Recognition Service
                // O Recognition retorna uma lista de subjects correspondentes
                String expectedSubject = userId.toString();
                boolean verified = false;
                double maxConfidence = 0.0;
                String foundSubject = null;
                
                // O CompreFace Recognition pode retornar a resposta em diferentes formatos
                // Vamos verificar todas as possibilidades
                
                // Formato 1: responseBody contém "result" que é uma lista
                if (responseBody.containsKey("result")) {
                    Object resultObj = responseBody.get("result");
                    logger.info("Result encontrado, tipo: {}", resultObj != null ? resultObj.getClass().getName() : "null");
                    
                    if (resultObj instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Map<String, Object>> results = (java.util.List<Map<String, Object>>) resultObj;
                        logger.info("Result é uma lista com {} itens", results.size());
                        
                        if (results.isEmpty()) {
                            logger.warn("⚠ Lista de resultados está VAZIA - nenhuma face foi reconhecida pelo CompreFace");
                            logger.warn("Isso pode indicar que:");
                            logger.warn("  1. A face não foi cadastrada corretamente");
                            logger.warn("  2. A qualidade da imagem é muito baixa");
                            logger.warn("  3. Não há rosto detectável na imagem");
                        }
                        
                        // Procurar se algum resultado corresponde ao subject esperado
                        // O formato do CompreFace é: result = [{box: {...}, subjects: [{subject: "...", similarity: ...}]}]
                        for (int i = 0; i < results.size(); i++) {
                            Map<String, Object> result = results.get(i);
                            logger.info("Result[{}]: {}", i, result);
                            
                            // O subject está dentro de "subjects" que é uma lista
                            Object subjectsObj = result.get("subjects");
                            logger.info("  - subjects encontrado: {}", subjectsObj);
                            
                            if (subjectsObj instanceof java.util.List) {
                                @SuppressWarnings("unchecked")
                                java.util.List<Map<String, Object>> subjects = (java.util.List<Map<String, Object>>) subjectsObj;
                                logger.info("  - subjects é uma lista com {} itens", subjects.size());
                                
                                // Iterar sobre cada subject na lista
                                for (int j = 0; j < subjects.size(); j++) {
                                    Map<String, Object> subjectMap = subjects.get(j);
                                    logger.info("  - Subject[{}]: {}", j, subjectMap);
                                    
                                    Object subjectObj = subjectMap.get("subject");
                                    Object similarityObj = subjectMap.get("similarity");
                                    Object distanceObj = subjectMap.get("distance");
                                    
                                    logger.info("    - subject: {}", subjectObj);
                                    logger.info("    - similarity: {}", similarityObj);
                                    logger.info("    - distance: {}", distanceObj);
                                    
                                    // Converter distance para similarity se necessário
                                    if (distanceObj != null && similarityObj == null) {
                                        if (distanceObj instanceof Number) {
                                            double distance = ((Number) distanceObj).doubleValue();
                                            similarityObj = 1.0 - distance;
                                            logger.info("    - similarity calculado de distance: {}", similarityObj);
                                        }
                                    }
                                    
                                    if (subjectObj != null) {
                                        String subjectStr = subjectObj.toString().trim();
                                        logger.info("    - Comparando subject '{}' (trimmed) com esperado '{}'", subjectStr, expectedSubject);
                                        
                                        // Comparação case-insensitive e com trim
                                        if (subjectStr.equalsIgnoreCase(expectedSubject) || subjectStr.equals(expectedSubject)) {
                                            verified = true;
                                            foundSubject = subjectStr;
                                            if (similarityObj instanceof Number) {
                                                maxConfidence = ((Number) similarityObj).doubleValue();
                                            }
                                            logger.info("    ✓ MATCH encontrado! similarity: {}", maxConfidence);
                                            break;
                                        } else {
                                            logger.info("    ✗ Subject não corresponde (comparação: '{}' != '{}')", subjectStr, expectedSubject);
                                        }
                                    }
                                    
                                    // Manter o maior nível de confiança encontrado
                                    if (similarityObj instanceof Number) {
                                        double similarity = ((Number) similarityObj).doubleValue();
                                        if (similarity > maxConfidence) {
                                            maxConfidence = similarity;
                                            foundSubject = subjectObj != null ? subjectObj.toString() : null;
                                        }
                                    }
                                }
                                
                                // Se encontrou match, sair do loop externo também
                                if (verified) {
                                    break;
                                }
                            } else {
                                logger.warn("  ⚠ subjects não é uma lista, tipo: {}", subjectsObj != null ? subjectsObj.getClass().getName() : "null");
                            }
                        }
                    } else if (resultObj instanceof Map) {
                        // Formato alternativo: result é um objeto único (pode ter subjects dentro)
                        Map<String, Object> result = (Map<String, Object>) resultObj;
                        logger.info("Result é um Map: {}", result);
                        
                        // Verificar se tem subjects dentro
                        Object subjectsObj = result.get("subjects");
                        if (subjectsObj instanceof java.util.List) {
                            @SuppressWarnings("unchecked")
                            java.util.List<Map<String, Object>> subjects = (java.util.List<Map<String, Object>>) subjectsObj;
                            
                            for (Map<String, Object> subjectMap : subjects) {
                                Object subjectObj = subjectMap.get("subject");
                                Object similarityObj = subjectMap.get("similarity");
                                
                                if (subjectObj != null && subjectObj.toString().trim().equalsIgnoreCase(expectedSubject)) {
                                    verified = true;
                                    foundSubject = subjectObj.toString();
                                    if (similarityObj instanceof Number) {
                                        maxConfidence = ((Number) similarityObj).doubleValue();
                                    }
                                    break;
                                }
                            }
                        } else {
                            // Formato antigo: subject diretamente no result
                            Object subjectObj = result.get("subject");
                            Object similarityObj = result.get("similarity");
                            
                            if (subjectObj != null && subjectObj.toString().trim().equalsIgnoreCase(expectedSubject)) {
                                verified = true;
                                foundSubject = subjectObj.toString();
                                if (similarityObj instanceof Number) {
                                    maxConfidence = ((Number) similarityObj).doubleValue();
                                }
                            }
                        }
                    }
                }
                
                // Formato 2: responseBody pode ter "subjects" diretamente (formato alternativo)
                if (!verified && responseBody.containsKey("subjects")) {
                    Object subjectsObj = responseBody.get("subjects");
                    logger.info("Subjects encontrado diretamente: {}", subjectsObj);
                    
                    if (subjectsObj instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Map<String, Object>> subjects = (java.util.List<Map<String, Object>>) subjectsObj;
                        logger.info("Subjects é uma lista com {} itens", subjects.size());
                        
                        for (Map<String, Object> subject : subjects) {
                            Object subjectName = subject.get("subject");
                            Object similarity = subject.get("similarity");
                            
                            if (subjectName != null && subjectName.toString().trim().equalsIgnoreCase(expectedSubject)) {
                                verified = true;
                                foundSubject = subjectName.toString();
                                if (similarity instanceof Number) {
                                    maxConfidence = ((Number) similarity).doubleValue();
                                }
                                logger.info("✓ MATCH encontrado em subjects! similarity: {}", maxConfidence);
                                break;
                            }
                        }
                    }
                }
                
                // Formato 3: Verificar se há "predictions" ou "matches" (outros formatos possíveis)
                if (!verified) {
                    logger.info("Verificando outros formatos possíveis na resposta...");
                    for (String key : responseBody.keySet()) {
                        logger.info("  - Chave encontrada: {}", key);
                    }
                }
                
                logger.info("=== Resultado final ===");
                logger.info("Expected subject: '{}'", expectedSubject);
                logger.info("Found subject: '{}'", foundSubject);
                logger.info("Verified: {}", verified);
                logger.info("Max confidence: {}", maxConfidence);
                
                // Se não encontrou correspondência exata, mas há resultados com alta confiança,
                // pode ser que o subject esteja em formato diferente
                if (!verified && foundSubject != null && maxConfidence > 0.7) {
                    logger.warn("⚠ ATENÇÃO: Encontrado subject '{}' com confiança {}, mas não corresponde ao esperado '{}'", 
                        foundSubject, maxConfidence, expectedSubject);
                    logger.warn("Verifique se o subject foi cadastrado corretamente no CompreFace");
                }
                
                verifyResponse.setVerified(verified);
                verifyResponse.setConfidence(maxConfidence);
                verifyResponse.setMessage(verified 
                    ? String.format("Face verificada com sucesso (confiança: %.2f%%)", maxConfidence * 100)
                    : String.format("Face não corresponde. Subject encontrado: %s, Confiança: %.2f%%", 
                        foundSubject != null ? foundSubject : "nenhum", maxConfidence * 100));
                
                logger.info("✓ Face do usuário {} verificada - verified: {}, confidence: {}", 
                    userId, verified, maxConfidence);
                return verifyResponse;
            } else {
                logger.warn("✗ Falha ao verificar face do usuário {} no CompreFace. Status: {}, Body: {}", 
                    userId, response.getStatusCode(), response.getBody());
                return null;
            }
            
        } catch (RestClientException e) {
            logger.error("✗ Erro ao verificar face do usuário {} no CompreFace", userId, e);
            logger.error("Detalhes do erro: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Causa: {}", e.getCause().getMessage());
            }
            return null;
        } catch (Exception e) {
            logger.error("✗ Erro inesperado ao verificar face do usuário {} no CompreFace: {}", userId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Verifica se uma face corresponde ao usuário cadastrado usando URL de imagem
     * 
     * @param userId ID do usuário para verificação
     * @param imageUrl URL da imagem contendo o rosto a verificar
     * @return FaceVerifyResponse com resultado da verificação, ou null em caso de erro
     */
    public FaceVerifyResponse verifyFaceFromUrl(UUID userId, String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            logger.warn("URL da imagem vazia para verificação do usuário {}.", userId);
            return null;
        }
        
        if (comprefaceApiKey == null || comprefaceApiKey.isEmpty()) {
            logger.error("API Key do CompreFace não configurada. Configure a propriedade compreface.api.key");
            return null;
        }
        
        try {
            // Garantir que não há barras duplas na URL
            String baseUrl = comprefaceApiUrl.endsWith("/") 
                ? comprefaceApiUrl.substring(0, comprefaceApiUrl.length() - 1) 
                : comprefaceApiUrl;
            String endpoint = verifyEndpoint.startsWith("/") 
                ? verifyEndpoint 
                : "/" + verifyEndpoint;
            String url = baseUrl + endpoint;
            
            // Preparar o body JSON (Recognition Service)
            Map<String, String> requestBody = Map.of(
                "url", imageUrl
            );
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", comprefaceApiKey);
            
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            logger.info("=== CompreFace - Verificar Face via URL (Recognition) ===");
            logger.info("URL completa: {}", url);
            logger.info("User ID esperado (subject): {}", userId);
            logger.info("Photo URL: {}", imageUrl);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(url, requestEntity, Map.class);
            
            logger.info("Resposta do CompreFace - Status: {}", response.getStatusCode());
            if (response.getBody() != null) {
                logger.info("Resposta do CompreFace - Body: {}", response.getBody());
            }
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                
                FaceVerifyResponse verifyResponse = new FaceVerifyResponse();
                verifyResponse.setUser_id(userId.toString());
                
                // Processar resposta do Recognition Service (mesma lógica do verifyFace)
                String expectedSubject = userId.toString().trim();
                boolean verified = false;
                double maxConfidence = 0.0;
                String foundSubject = null;
                
                if (responseBody.containsKey("result")) {
                    Object resultObj = responseBody.get("result");
                    if (resultObj instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Map<String, Object>> results = (java.util.List<Map<String, Object>>) resultObj;
                        
                        // O formato do CompreFace é: result = [{box: {...}, subjects: [{subject: "...", similarity: ...}]}]
                        for (Map<String, Object> result : results) {
                            Object subjectsObj = result.get("subjects");
                            
                            if (subjectsObj instanceof java.util.List) {
                                @SuppressWarnings("unchecked")
                                java.util.List<Map<String, Object>> subjects = (java.util.List<Map<String, Object>>) subjectsObj;
                                
                                for (Map<String, Object> subjectMap : subjects) {
                                    Object subjectObj = subjectMap.get("subject");
                                    Object similarityObj = subjectMap.get("similarity");
                                    
                                    if (subjectObj != null && subjectObj.toString().trim().equalsIgnoreCase(expectedSubject)) {
                                        verified = true;
                                        foundSubject = subjectObj.toString();
                                        if (similarityObj instanceof Number) {
                                            maxConfidence = ((Number) similarityObj).doubleValue();
                                        }
                                        break;
                                    } else if (similarityObj instanceof Number) {
                                        double similarity = ((Number) similarityObj).doubleValue();
                                        if (similarity > maxConfidence) {
                                            maxConfidence = similarity;
                                            foundSubject = subjectObj != null ? subjectObj.toString() : null;
                                        }
                                    }
                                }
                                
                                if (verified) {
                                    break;
                                }
                            }
                        }
                    } else if (resultObj instanceof Map) {
                        Map<String, Object> result = (Map<String, Object>) resultObj;
                        Object subjectsObj = result.get("subjects");
                        
                        if (subjectsObj instanceof java.util.List) {
                            @SuppressWarnings("unchecked")
                            java.util.List<Map<String, Object>> subjects = (java.util.List<Map<String, Object>>) subjectsObj;
                            
                            for (Map<String, Object> subjectMap : subjects) {
                                Object subjectObj = subjectMap.get("subject");
                                Object similarityObj = subjectMap.get("similarity");
                                
                                if (subjectObj != null && subjectObj.toString().trim().equalsIgnoreCase(expectedSubject)) {
                                    verified = true;
                                    foundSubject = subjectObj.toString();
                                    if (similarityObj instanceof Number) {
                                        maxConfidence = ((Number) similarityObj).doubleValue();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                
                verifyResponse.setVerified(verified);
                verifyResponse.setConfidence(maxConfidence);
                verifyResponse.setMessage(verified 
                    ? String.format("Face verificada com sucesso (confiança: %.2f%%)", maxConfidence * 100)
                    : String.format("Face não corresponde. Subject encontrado: %s, Confiança: %.2f%%", 
                        foundSubject != null ? foundSubject : "nenhum", maxConfidence * 100));
                
                logger.info("✓ Face do usuário {} verificada via URL - verified: {}, confidence: {}", 
                    userId, verified, maxConfidence);
                return verifyResponse;
            } else {
                logger.warn("✗ Falha ao verificar face do usuário {} no CompreFace via URL. Status: {}, Body: {}", 
                    userId, response.getStatusCode(), response.getBody());
                return null;
            }
            
        } catch (RestClientException e) {
            logger.error("✗ Erro ao verificar face do usuário {} no CompreFace via URL", userId, e);
            logger.error("Detalhes do erro: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Causa: {}", e.getCause().getMessage());
            }
            return null;
        } catch (Exception e) {
            logger.error("✗ Erro inesperado ao verificar face do usuário {} no CompreFace via URL: {}", userId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Classe para resposta de verificação de face
     */
    public static class FaceVerifyResponse {
        private boolean verified;
        private double confidence;
        private String message;
        private String user_id;

        public FaceVerifyResponse() {}

        public boolean isVerified() {
            return verified;
        }

        public void setVerified(boolean verified) {
            this.verified = verified;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }
    }
    
    /**
     * Classe auxiliar para converter MultipartFile em Resource
     */
    private static class MultipartFileResource extends ByteArrayResource {
        private final String filename;
        
        public MultipartFileResource(MultipartFile multipartFile) {
            super(getBytes(multipartFile));
            this.filename = multipartFile.getOriginalFilename();
        }
        
        private static byte[] getBytes(MultipartFile multipartFile) {
            try {
                return multipartFile.getBytes();
            } catch (Exception e) {
                throw new RuntimeException("Erro ao ler bytes do arquivo", e);
            }
        }
        
        @Override
        public String getFilename() {
            return filename;
        }
    }
}
