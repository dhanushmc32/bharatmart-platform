package com.bharatmart.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public record AuthenticatedUser(Long userId, String username, List<String> roles, String jti) {
    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser u) {
            return u.userId();
        }
        throw new IllegalStateException("No authenticated user in security context");
    }
}
