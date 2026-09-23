package com.auth_service.controller;

import com.auth_service.dto.CreateUserDTO;
import com.auth_service.dto.UserResponseDTO;
import com.auth_service.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/createUser")
    public ResponseEntity<UserResponseDTO> createOrFindUser(@RequestBody CreateUserDTO createUserDTO) {
        UserService.FindOrCreateResult result = userService.findOrCreate(createUserDTO);
        UserResponseDTO body = UserResponseDTO.fromEntity(result.user());

        return result.created()
                ? ResponseEntity.status(HttpStatus.CREATED).body(body)
                : ResponseEntity.ok(body);
    }

}