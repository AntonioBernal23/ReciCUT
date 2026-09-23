package com.auth_service.events;

import com.auth_service.dto.CreateUserDTO;
import com.auth_service.entity.UserEntity;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {

    private static final String TOPIC = "user-registered";

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Registro tradicional
    public void publishUserRegistered(UserEntity user, CreateUserDTO dto) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                dto.getName(),
                dto.getLastName(),
                null // no hay foto
        );

        kafkaTemplate.send(TOPIC, user.getId().toString(), event);
    }

    // Registro con Google
    public void publishUserRegisteredFromGoogle(UserEntity user, GoogleIdToken.Payload payload) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                (String) payload.get("given_name"),
                (String) payload.get("family_name"),
                (String) payload.get("picture")
        );

        kafkaTemplate.send(TOPIC, user.getId().toString(), event);
    }
}