package com.bharatmart.inventory.repository;

import com.bharatmart.inventory.entity.InventoryHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryHistoryRepository extends JpaRepository<InventoryHistoryEntity, Long> {}
