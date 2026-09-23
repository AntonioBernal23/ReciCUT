package com.auth_service.service;

import com.auth_service.entity.UserEntity;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleAuthService {

    private final JwtService jwtService;
    private final UserService userService;

    public GoogleAuthService(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Value("${google.client-id}")
    private String googleClientId;

    public ResponseEntity<String> verify(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new RuntimeException("Token de Google inválido");
            }

            UserService.FindOrCreateResult result = userService.findOrCreateGoogleUser(idToken.getPayload());
            UserEntity user = result.user();

            String token = jwtService.generateToken(user.getEmail(), user.getRole());

            return ResponseEntity.ok(token);

        } catch (Exception e) {
            throw new RuntimeException("Error verificando token de Google: " + e.getMessage());
        }
    }
}