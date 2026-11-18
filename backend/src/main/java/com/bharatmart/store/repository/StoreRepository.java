package com.bharatmart.store.repository;

import com.bharatmart.store.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
    Optional<StoreEntity> findByStoreCode(String storeCode);
    List<StoreEntity> findByActiveTrue();
    List<StoreEntity> findByCityCodeIgnoreCaseAndActiveTrue(String cityCode);
    List<StoreEntity> findByStateCodeIgnoreCaseAndActiveTrue(String stateCode);
}
