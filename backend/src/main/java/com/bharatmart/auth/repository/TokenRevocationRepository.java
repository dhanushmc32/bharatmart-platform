package com.bharatmart.auth.repository;

import com.bharatmart.auth.entity.TokenRevocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRevocationRepository extends JpaRepository<TokenRevocationEntity, Long> {
    boolean existsByTokenJti(String tokenJti);
}
