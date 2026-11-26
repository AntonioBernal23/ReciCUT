package com.user_service.controller;

import com.user_service.Repository.ProfileRepository;
import com.user_service.entity.ProfileEntity;
import com.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ProfileRepository profileRepository;

    public UserController(UserService userService, ProfileRepository profileRepository) {
        this.userService = userService;
        this.profileRepository = profileRepository;
    }

    @GetMapping("/getUser/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userService.findUser(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long id) {
        Optional<ProfileEntity> profileOpt = profileRepository.findByUserId(id);
        if (profileOpt.isEmpty()) {
            profileOpt = profileRepository.findById(id);
        }
        if (profileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ProfileEntity profile = profileOpt.get();
        String nombreCompleto = (profile.getName() != null ? profile.getName() : "")
                + (profile.getLastName() != null && !profile.getLastName().isBlank()
                ? " " + profile.getLastName() : "");

        Map<String, Object> response = new HashMap<>();
        response.put("id", profile.getUserId());
        response.put("nombre", nombreCompleto.trim());
        response.put("email", null);

        return ResponseEntity.ok(response);
    }
}