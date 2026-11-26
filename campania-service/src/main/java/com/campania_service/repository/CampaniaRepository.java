package com.campania_service.repository;

import com.campania_service.entity.CampaniaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaniaRepository extends JpaRepository<CampaniaEntity, Long> {
}
