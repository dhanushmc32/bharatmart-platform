package com.bharatmart.product.service;

import com.bharatmart.common.exception.ResourceNotFoundException;
import com.bharatmart.product.dto.ProductResponse;
import com.bharatmart.product.dto.SkuResponse;
import com.bharatmart.product.entity.CategoryEntity;
import com.bharatmart.product.entity.ProductEntity;
import com.bharatmart.product.repository.CategoryRepository;
import com.bharatmart.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryEntity> listCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String query, String categorySlug) {
        List<ProductEntity> products;
        if (query != null && !query.isBlank()) {
            products = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(query.trim());
        } else if (categorySlug != null && !categorySlug.isBlank()) {
            products = productRepository.findByCategorySlugAndActiveTrue(categorySlug.trim());
        } else {
            products = productRepository.findByActiveTrue();
        }
        return products.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return toResponse(product);
    }

    private ProductResponse toResponse(ProductEntity p) {
        List<SkuResponse> skus = p.getSkus().stream()
                .filter(s -> s.isActive())
                .map(s -> new SkuResponse(s.getId(), s.getSkuCode(), s.getVariantName(), s.getMrp(), s.getSellingPrice()))
                .toList();
        return new ProductResponse(
                p.getId(), p.getName(), p.getSlug(), p.getBrand(),
                p.getCategory().getName(), p.getCategory().getSlug(),
                p.getDescription(), p.getImageUrl(), skus
        );
    }
}
