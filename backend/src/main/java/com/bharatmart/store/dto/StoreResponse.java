package com.bharatmart.store.dto;

public record StoreResponse(
        Long id,
        String storeCode,
        String name,
        String stateCode,
        String stateName,
        String cityCode,
        String cityName,
        String addressLine,
        String pincode,
        String contactPhone,
        boolean active
) {}
