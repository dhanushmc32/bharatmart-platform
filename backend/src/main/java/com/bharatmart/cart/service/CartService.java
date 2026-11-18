package com.bharatmart.cart.service;

import com.bharatmart.cart.dto.AddToCartRequest;
import com.bharatmart.cart.dto.CartResponse;
import com.bharatmart.cart.entity.CartItemEntity;
import com.bharatmart.cart.entity.ShoppingCartEntity;
import com.bharatmart.cart.repository.ShoppingCartRepository;
import com.bharatmart.common.exception.ResourceNotFoundException;
import com.bharatmart.product.entity.SkuEntity;
import com.bharatmart.product.repository.SkuRepository;
import com.bharatmart.store.entity.StoreEntity;
import com.bharatmart.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ShoppingCartRepository cartRepository;
    private final StoreService storeService;
    private final SkuRepository skuRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId, String storeCode) {
        ShoppingCartEntity cart = cartRepository.findByUserIdAndStoreStoreCode(userId, storeCode)
                .orElse(null);
        if (cart == null) {
            return new CartResponse(null, storeCode, List.of(), BigDecimal.ZERO);
        }
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(Long userId, AddToCartRequest req) {
        StoreEntity store = storeService.getStoreEntityByCode(req.storeCode());
        SkuEntity sku = skuRepository.findById(req.skuId())
                .orElseThrow(() -> new ResourceNotFoundException("SKU not found: " + req.skuId()));

        ShoppingCartEntity cart = cartRepository.findByUserIdAndStoreStoreCode(userId, req.storeCode())
                .orElseGet(() -> cartRepository.save(ShoppingCartEntity.builder()
                        .userId(userId)
                        .store(store)
                        .build()));

        CartItemEntity existing = cart.getItems().stream()
                .filter(i -> i.getSku().getId().equals(sku.getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + req.quantity());
        } else {
            cart.getItems().add(CartItemEntity.builder()
                    .cart(cart)
                    .sku(sku)
                    .quantity(req.quantity())
                    .build());
        }
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(Long userId, String storeCode, Long skuId) {
        ShoppingCartEntity cart = cartRepository.findByUserIdAndStoreStoreCode(userId, storeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for store: " + storeCode));
        cart.getItems().removeIf(i -> i.getSku().getId().equals(skuId));
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(Long userId, String storeCode) {
        cartRepository.findByUserIdAndStoreStoreCode(userId, storeCode)
                .ifPresent(c -> {
                    c.getItems().clear();
                    cartRepository.save(c);
                });
    }

    private CartResponse toResponse(ShoppingCartEntity cart) {
        List<CartResponse.CartItemView> itemViews = cart.getItems().stream().map(i -> {
            BigDecimal lineTotal = i.getSku().getSellingPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
            return new CartResponse.CartItemView(
                    i.getId(), i.getSku().getId(), i.getSku().getSkuCode(),
                    i.getSku().getProduct().getName(), i.getSku().getVariantName(),
                    i.getSku().getSellingPrice(), i.getQuantity(), lineTotal
            );
        }).toList();

        BigDecimal subtotal = itemViews.stream()
                .map(CartResponse.CartItemView::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), cart.getStore().getStoreCode(), itemViews, subtotal);
    }
}
