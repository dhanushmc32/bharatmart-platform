package com.bharatmart.cart.repository;

import com.bharatmart.cart.entity.ShoppingCartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCartEntity, Long> {
    Optional<ShoppingCartEntity> findByUserIdAndStoreStoreCode(Long userId, String storeCode);
}
