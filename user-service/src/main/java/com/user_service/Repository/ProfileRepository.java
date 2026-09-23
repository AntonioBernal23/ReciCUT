package com.user_service.Repository;

import com.user_service.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
    boolean existsByUserId(Long userId);
    Optional<ProfileEntity> findByUserId(Long userId);
}