package com.auth_service.dto;

public record LoginResponseDTO(
        String token,
        UserResponseDTO user
) {}