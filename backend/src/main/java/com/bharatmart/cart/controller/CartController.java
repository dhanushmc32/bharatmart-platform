package com.bharatmart.cart.controller;

import com.bharatmart.auth.security.AuthenticatedUser;
import com.bharatmart.cart.dto.AddToCartRequest;
import com.bharatmart.cart.dto.CartResponse;
import com.bharatmart.cart.service.CartService;
import com.bharatmart.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@RequestParam String storeCode) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(AuthenticatedUser.currentUserId(), storeCode)));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Item added to cart",
                cartService.addItem(AuthenticatedUser.currentUserId(), request)));
    }

    @DeleteMapping("/items/{skuId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @RequestParam String storeCode,
            @PathVariable Long skuId) {
        return ResponseEntity.ok(ApiResponse.ok("Item removed",
                cartService.removeItem(AuthenticatedUser.currentUserId(), storeCode, skuId)));
    }
}
