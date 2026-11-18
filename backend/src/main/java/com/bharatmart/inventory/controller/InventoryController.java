package com.bharatmart.inventory.controller;

import com.bharatmart.common.ApiResponse;
import com.bharatmart.inventory.dto.StoreInventoryResponse;
import com.bharatmart.inventory.dto.UpdateStockRequest;
import com.bharatmart.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores/{storeCode}/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreInventoryResponse>>> getStoreInventory(@PathVariable String storeCode) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getStoreInventory(storeCode)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<StoreInventoryResponse>>> getLowStock(@PathVariable String storeCode) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getLowStockAlerts(storeCode)));
    }

    @PutMapping("/{skuId}")
    public ResponseEntity<ApiResponse<StoreInventoryResponse>> updateStock(
            @PathVariable String storeCode,
            @PathVariable Long skuId,
            @Valid @RequestBody UpdateStockRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Stock updated", inventoryService.updateStock(storeCode, skuId, request)));
    }
}
