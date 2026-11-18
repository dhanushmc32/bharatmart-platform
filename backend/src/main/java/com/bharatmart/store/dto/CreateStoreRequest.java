package com.bharatmart.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateStoreRequest(
        @NotBlank String name,
        @NotBlank String stateCode,
        @NotBlank String cityCode,
        @NotBlank String addressLine,
        @NotBlank @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid Indian 6-digit PIN code") String pincode,
        @NotBlank String contactPhone,
        Long managerUserId
) {}
