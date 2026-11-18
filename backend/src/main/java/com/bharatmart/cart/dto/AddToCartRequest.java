package com.bharatmart.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotBlank String storeCode,
        @NotNull Long skuId,
        @Min(1) int quantity
) {}
