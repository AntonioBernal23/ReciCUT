package com.auth_service.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateUserDTO {

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String name;

    private String lastName;

}
