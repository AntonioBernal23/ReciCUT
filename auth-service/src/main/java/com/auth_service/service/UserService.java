package com.auth_service.service;

import com.auth_service.dto.CreateUserDTO;
import com.auth_service.entity.UserEntity;
import com.auth_service.events.UserEventProducer;
import com.auth_service.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventProducer userEventProducer;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userEventProducer = userEventProducer;
    }

    public record FindOrCreateResult(UserEntity user, boolean created) {}

    public FindOrCreateResult findOrCreate(CreateUserDTO createUserDTO) {
        return userRepository.findByEmail(createUserDTO.getEmail())
                .map(existing -> new FindOrCreateResult(existing, false))
                .orElseGet(() -> {
                    UserEntity newUser = userRepository.save(
                            UserEntity.builder()
                                    .email(createUserDTO.getEmail())
                                    .password(passwordEncoder.encode(createUserDTO.getPassword()))
                                    .role(UserEntity.Role.USER)
                                    .authProvider(UserEntity.AuthProvider.LOCAL)
                                    .createdAt(LocalDateTime.now())
                                    .build()
                    );
                    userEventProducer.publishUserRegistered(newUser, createUserDTO);
                    return new FindOrCreateResult(newUser, true);
                });
    }

    public FindOrCreateResult findOrCreateGoogleUser(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();

        return userRepository.findByEmail(email)
                .map(existing -> new FindOrCreateResult(existing, false))
                .orElseGet(() -> {
                    UserEntity newUser = userRepository.save(
                            UserEntity.builder()
                                    .email(email)
                                    .password(null)
                                    .role(UserEntity.Role.USER)
                                    .authProvider(UserEntity.AuthProvider.GOOGLE)
                                    .createdAt(LocalDateTime.now())
                                    .build()
                    );

                    userEventProducer.publishUserRegisteredFromGoogle(newUser, payload);
                    return new FindOrCreateResult(newUser, true);
                });
    }
}