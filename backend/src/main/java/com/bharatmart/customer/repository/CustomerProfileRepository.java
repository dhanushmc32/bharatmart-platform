package com.bharatmart.customer.repository;

import com.bharatmart.customer.entity.CustomerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfileEntity, Long> {
    Optional<CustomerProfileEntity> findByUserId(Long userId);
}
