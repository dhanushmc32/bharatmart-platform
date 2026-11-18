package com.bharatmart.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        String storeCode,
        List<CartItemView> items,
        BigDecimal subtotal
) {
    public record CartItemView(
            Long itemId,
            Long skuId,
            String skuCode,
            String productName,
            String variantName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {}
}
