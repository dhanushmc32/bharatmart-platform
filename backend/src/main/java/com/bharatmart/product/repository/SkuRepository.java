package com.bharatmart.product.repository;

import com.bharatmart.product.entity.SkuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SkuRepository extends JpaRepository<SkuEntity, Long> {
    Optional<SkuEntity> findBySkuCode(String skuCode);
}
