package com.bharatmart.payment.controller;

import com.bharatmart.common.ApiResponse;
import com.bharatmart.payment.entity.PaymentEntity;
import com.bharatmart.payment.service.CodPaymentService;
import com.bharatmart.payment.service.CodPaymentService.UpdateCodStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CodPaymentController {

    private final CodPaymentService codPaymentService;

    @PatchMapping("/api/v1/store/payments/{orderId}/cod")
    public ResponseEntity<ApiResponse<PaymentEntity>> updateCodStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateCodStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("COD payment status updated",
                codPaymentService.updateCodStatus(orderId, request)));
    }

    @GetMapping("/api/v1/admin/payments")
    public ResponseEntity<ApiResponse<List<PaymentEntity>>> listAllPayments() {
        return ResponseEntity.ok(ApiResponse.ok(codPaymentService.listAllPayments()));
    }
}
