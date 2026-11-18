package com.bharatmart.store.repository;

import com.bharatmart.store.entity.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<CityEntity, Long> {
    List<CityEntity> findByStateId(Long stateId);
    Optional<CityEntity> findByStateIdAndCodeIgnoreCase(Long stateId, String code);
}
