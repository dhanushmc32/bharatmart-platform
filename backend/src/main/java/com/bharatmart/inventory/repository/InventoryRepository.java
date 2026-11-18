package com.bharatmart.inventory.repository;

import com.bharatmart.inventory.entity.InventoryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

    List<InventoryEntity> findByStoreStoreCode(String storeCode);

    Optional<InventoryEntity> findByStoreIdAndSkuId(Long storeId, Long skuId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryEntity i WHERE i.store.id = :storeId AND i.sku.id = :skuId")
    Optional<InventoryEntity> findByStoreIdAndSkuIdForUpdate(@Param("storeId") Long storeId, @Param("skuId") Long skuId);

    @Query("SELECT i FROM InventoryEntity i WHERE i.store.storeCode = :storeCode AND i.availableQty <= i.reorderLevel")
    List<InventoryEntity> findLowStockByStoreCode(@Param("storeCode") String storeCode);
}
