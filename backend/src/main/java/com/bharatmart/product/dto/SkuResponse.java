package com.bharatmart.product.dto;

import java.math.BigDecimal;
import java.util.List;

public record SkuResponse(
        Long id,
        String skuCode,
        String variantName,
        BigDecimal mrp,
        BigDecimal sellingPrice
) {}
