package com.bharatmart.store.controller;

import com.bharatmart.common.ApiResponse;
import com.bharatmart.store.dto.CreateStoreRequest;
import com.bharatmart.store.dto.StoreResponse;
import com.bharatmart.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreResponse>>> listStores(
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String cityCode) {
        return ResponseEntity.ok(ApiResponse.ok(storeService.listActiveStores(stateCode, cityCode)));
    }

    @GetMapping("/{storeCode}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStore(@PathVariable String storeCode) {
        return ResponseEntity.ok(ApiResponse.ok(storeService.getByStoreCode(storeCode)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@Valid @RequestBody CreateStoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Store registered", storeService.createStore(request)));
    }
}
