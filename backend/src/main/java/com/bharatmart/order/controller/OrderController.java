package com.bharatmart.order.controller;

import com.bharatmart.auth.security.AuthenticatedUser;
import com.bharatmart.common.ApiResponse;
import com.bharatmart.order.dto.OrderResponse;
import com.bharatmart.order.dto.PlaceOrderRequest;
import com.bharatmart.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("COD order placed successfully",
                        orderService.placeOrder(AuthenticatedUser.currentUserId(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> myOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getUserOrders(AuthenticatedUser.currentUserId())));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrder(orderNumber)));
    }

    @PostMapping("/{orderNumber}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled and stock released", orderService.cancelOrder(orderNumber)));
    }
}
