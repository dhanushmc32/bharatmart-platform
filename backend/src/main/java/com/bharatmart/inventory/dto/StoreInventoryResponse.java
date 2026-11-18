package com.bharatmart.inventory.dto;

import java.math.BigDecimal;

public record StoreInventoryResponse(
        Long inventoryId,
        String storeCode,
        Long skuId,
        String skuCode,
        String productName,
        String variantName,
        BigDecimal sellingPrice,
        int availableQty,
        int reservedQty,
        int reorderLevel,
        boolean lowStock
) {}
