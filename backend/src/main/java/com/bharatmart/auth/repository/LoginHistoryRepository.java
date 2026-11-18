package com.bharatmart.auth.repository;

import com.bharatmart.auth.entity.LoginHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoginHistoryRepository extends JpaRepository<LoginHistoryEntity, Long> {
    Optional<LoginHistoryEntity> findByTokenJti(String tokenJti);
}
