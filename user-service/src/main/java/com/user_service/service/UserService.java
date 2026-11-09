package com.user_service.service;

import com.user_service.Repository.ProfileRepository;
import com.user_service.dto.UserResponseDto;
import com.user_service.entity.ProfileEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final ProfileRepository profileRepository;

    public UserService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public ResponseEntity<UserResponseDto> findUser(Long id) {
        Optional<ProfileEntity> profileEntity = profileRepository.findByUserId(id);

        ProfileEntity profile = profileEntity.orElseThrow(
                () -> new RuntimeException("Perfil no encontrado")
        );

        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setName(profile.getName());
        userResponseDto.setLastName(profile.getLastName());
        userResponseDto.setPicture(profile.getPicture());

        return ResponseEntity.ok().body(userResponseDto);
    }

}
