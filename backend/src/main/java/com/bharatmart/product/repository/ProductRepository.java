package com.bharatmart.product.repository;

import com.bharatmart.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    List<ProductEntity> findByActiveTrue();
    List<ProductEntity> findByCategorySlugAndActiveTrue(String categorySlug);
    List<ProductEntity> findByNameContainingIgnoreCaseAndActiveTrue(String query);
}
