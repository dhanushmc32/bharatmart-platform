package com.bharatmart.order.dto;

import com.bharatmart.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
        @NotBlank String storeCode,
        @NotBlank String shippingAddress,
        @NotNull PaymentMethod paymentMethod,
        String notes
) {}
