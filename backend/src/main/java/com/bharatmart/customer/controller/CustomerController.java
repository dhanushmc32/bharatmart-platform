package com.bharatmart.customer.controller;

import com.bharatmart.auth.security.AuthenticatedUser;
import com.bharatmart.common.ApiResponse;
import com.bharatmart.customer.service.CustomerService;
import com.bharatmart.customer.service.CustomerService.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers/me")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileView>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getProfile(AuthenticatedUser.currentUserId())));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ProfileView>> updateMyProfile(@Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated",
                customerService.updateProfile(AuthenticatedUser.currentUserId(), req)));
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<ProfileView>> addAddress(@Valid @RequestBody AddAddressRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Address added",
                customerService.addAddress(AuthenticatedUser.currentUserId(), req)));
    }
}
