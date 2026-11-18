package com.bharatmart.order.dto;

import com.bharatmart.order.entity.OrderStatus;
import com.bharatmart.payment.entity.PaymentMethod;
import com.bharatmart.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        String storeCode,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal totalAmount,
        String shippingAddress,
        String correlationId,
        Instant createdAt,
        List<OrderItemView> items
) {
    public record OrderItemView(
            String skuCode,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}
}
