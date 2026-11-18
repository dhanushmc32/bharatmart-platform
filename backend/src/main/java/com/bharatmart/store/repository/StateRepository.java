package com.bharatmart.store.repository;

import com.bharatmart.store.entity.StateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StateRepository extends JpaRepository<StateEntity, Long> {
    Optional<StateEntity> findByCodeIgnoreCase(String code);
}
