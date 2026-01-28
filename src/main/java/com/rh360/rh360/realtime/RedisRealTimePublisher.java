package com.rh360.rh360.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisRealTimePublisher implements RealTimePublisher {

    public static final String CHANNEL = "rh360:realtime";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisRealTimePublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(RealTimeEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(CHANNEL, payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro serializando evento realtime", e);
        }
    }
}

