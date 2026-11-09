package com.auth_service.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateUserDTO {

    @Column(unique = true, nullable = false)
    @NotBlank
    @Email
    private String email;
    @NotBlank @Size(min = 8) private String password;
    @NotBlank private String name;
    @NotBlank private String lastName;

}
