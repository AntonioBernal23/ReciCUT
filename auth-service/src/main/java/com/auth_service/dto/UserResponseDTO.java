package com.auth_service.dto;

import com.auth_service.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class UserResponseDTO {
    Long id;
    String email;
    UserEntity.Role role;
    UserEntity.AuthProvider authProvider;
    LocalDateTime createdAt;

    public static UserResponseDTO fromEntity(UserEntity user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getAuthProvider(),
                user.getCreatedAt()
        );
    }
}
