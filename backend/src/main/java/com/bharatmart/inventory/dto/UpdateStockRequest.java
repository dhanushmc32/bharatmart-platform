package com.bharatmart.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateStockRequest(
        @Min(0) int availableQty,
        @NotBlank String reason
) {}
