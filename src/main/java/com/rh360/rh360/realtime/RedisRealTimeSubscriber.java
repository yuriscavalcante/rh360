package com.rh360.rh360.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rh360.rh360.dto.UserResponse;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.service.UsersService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RedisRealTimeSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final UsersService usersService;

    public RedisRealTimeSubscriber(ObjectMapper objectMapper,
                                   SimpMessagingTemplate messagingTemplate,
                                   UsersService usersService) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.usersService = usersService;
    }

    public void onMessage(String message) {
        try {
            RealTimeEvent event = objectMapper.readValue(message, RealTimeEvent.class);

            if (event.getTopic() == RealTimeTopic.USERS_ME && event.getUserId() != null) {
                UUID userId = UUID.fromString(event.getUserId());
                User user = usersService.findById(userId);
                if (user != null) {
                    messagingTemplate.convertAndSendToUser(
                            userId.toString(),
                            "/queue/users/me",
                            new UserResponse(user)
                    );
                }
                return;
            }

            // Para listagens: broadcast de "refresh" (front pode reconsultar via WS)
            if (event.getTopic() != null) {
                messagingTemplate.convertAndSend("/topic/" + event.getTopic().topic(), event);
            }
        } catch (Exception e) {
            // não derruba listener
        }
    }
}

