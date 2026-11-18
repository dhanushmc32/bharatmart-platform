package com.bharatmart.integration.controller;

import com.bharatmart.common.ApiResponse;
import com.bharatmart.integration.entity.MessageProcessingHistoryEntity;
import com.bharatmart.integration.entity.XmlMessageEntity;
import com.bharatmart.integration.service.XmlIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/middleware/messages")
@RequiredArgsConstructor
public class MiddlewareMonitorController {

    private final XmlIntegrationService integrationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<XmlMessageEntity>>> searchMessages(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(integrationService.searchMessages(status, orderId, q)));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<MessageProcessingHistoryEntity>>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(integrationService.getProcessingHistory(id)));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<ApiResponse<XmlMessageEntity>> retryMessage(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Retry executed", integrationService.retryMessage(id)));
    }
}
