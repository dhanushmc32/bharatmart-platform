package com.bharatmart.product.dto;

import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String brand,
        String categoryName,
        String categorySlug,
        String description,
        String imageUrl,
        List<SkuResponse> skus
) {}
