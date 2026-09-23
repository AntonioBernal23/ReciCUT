package com.user_service.events;

import com.user_service.Repository.ProfileRepository;
import com.user_service.entity.ProfileEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredListener {

    private final ProfileRepository profileRepository;

    public UserRegisteredListener(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @KafkaListener(topics = "user-registered", groupId = "user-service")
    public void handle(UserRegisteredEvent event) {
        if (profileRepository.existsByUserId(event.userId())) {
            return; // evita duplicar si Kafka reentrega el mensaje
        }

        ProfileEntity profile = ProfileEntity.builder()
                .userId(event.userId())
                .name(event.name())
                .lastName(event.lastName())
                .picture(event.picture())
                .build();

        profileRepository.save(profile);
    }
}