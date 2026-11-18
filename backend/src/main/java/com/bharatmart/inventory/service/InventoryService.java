package com.bharatmart.inventory.service;

import com.bharatmart.inventory.dto.StoreInventoryResponse;
import com.bharatmart.inventory.dto.UpdateStockRequest;
import java.util.List;

public interface InventoryService {
    List<StoreInventoryResponse> getStoreInventory(String storeCode);
    List<StoreInventoryResponse> getLowStockAlerts(String storeCode);
    StoreInventoryResponse updateStock(String storeCode, Long skuId, UpdateStockRequest request);
    void reserveStock(Long storeId, Long skuId, int qty, String orderNumber);
    void releaseStock(Long storeId, Long skuId, int qty, String orderNumber);
}
