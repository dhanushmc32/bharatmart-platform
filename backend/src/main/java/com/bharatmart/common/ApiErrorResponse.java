package com.bharatmart.common;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiErrorResponse(
        boolean success,
        int status,
        String errorCode,
        String message,
        String path,
        Map<String, String> validationErrors,
        OffsetDateTime timestamp
) {
    public static ApiErrorResponse of(int status, String errorCode, String message, String path) {
        return new ApiErrorResponse(false, status, errorCode, message, path, null, OffsetDateTime.now());
    }

    public static ApiErrorResponse validation(String message, String path, Map<String, String> errors) {
        return new ApiErrorResponse(false, 400, "VALIDATION_ERROR", message, path, errors, OffsetDateTime.now());
    }
}
