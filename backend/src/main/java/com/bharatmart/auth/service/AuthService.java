package com.bharatmart.auth.service;

import com.bharatmart.auth.dto.AuthDTOs.*;
import com.bharatmart.auth.entity.*;
import com.bharatmart.auth.repository.*;
import com.bharatmart.auth.security.AuthenticatedUser;
import com.bharatmart.auth.security.JwtService;
import com.bharatmart.common.exception.BusinessException;
import com.bharatmart.common.exception.ResourceNotFoundException;
import com.bharatmart.customer.entity.CustomerProfileEntity;
import com.bharatmart.customer.repository.CustomerProfileRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final TokenRevocationRepository revocationRepository;
    private final CustomerProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException("USERNAME_TAKEN", "Username already exists");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessException("EMAIL_TAKEN", "Email already registered");
        }

        RoleEntity customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER role not seeded"));

        UserEntity user = UserEntity.builder()
                .username(req.username().trim())
                .email(req.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.password()))
                .accountStatus("ACTIVE")
                .build();
        user.getRoles().add(customerRole);
        user = userRepository.save(user);

        profileRepository.save(CustomerProfileEntity.builder()
                .userId(user.getId())
                .fullName(req.fullName().trim())
                .phone(req.phone())
                .build());

        return issueTokens(user, "REGISTER", null);
    }

    @Transactional
    public AuthResponse login(LoginRequest req, String ipAddress) {
        UserEntity user = userRepository.findByUsernameOrEmail(req.identifier().trim(), req.identifier().trim().toLowerCase())
                .orElse(null);

        if (user == null) {
            loginHistoryRepository.save(LoginHistoryEntity.builder()
                    .identifierUsed(req.identifier())
                    .loginStatus("FAILED")
                    .ipAddress(ipAddress)
                    .failureReason("Unknown user")
                    .build());
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid username/email or password");
        }

        if ("LOCKED".equals(user.getAccountStatus()) && user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new BusinessException("ACCOUNT_LOCKED", "Account temporarily locked due to failed attempts");
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= 5) {
                user.setAccountStatus("LOCKED");
                user.setLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES));
            }
            userRepository.save(user);
            loginHistoryRepository.save(LoginHistoryEntity.builder()
                    .userId(user.getId())
                    .identifierUsed(req.identifier())
                    .loginStatus(attempts >= 5 ? "LOCKED" : "FAILED")
                    .ipAddress(ipAddress)
                    .failureReason("Bad password")
                    .build());
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid username/email or password");
        }

        user.setFailedAttempts(0);
        user.setAccountStatus("ACTIVE");
        user.setLockedUntil(null);
        userRepository.save(user);

        return issueTokens(user, req.identifier(), ipAddress);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        Claims claims = jwtService.parseClaims(req.refreshToken());
        if (!"REFRESH".equals(claims.get("type", String.class))) {
            throw new BusinessException("INVALID_TOKEN", "Expected refresh token");
        }
        String jti = claims.getId();
        if (revocationRepository.existsByTokenJti(jti)) {
            throw new BusinessException("TOKEN_REVOKED", "Refresh token has been revoked");
        }

        Long userId = claims.get("userId", Number.class).longValue();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        revocationRepository.save(TokenRevocationEntity.builder()
                .tokenJti(jti)
                .userId(userId)
                .expiresAt(claims.getExpiration().toInstant())
                .reason("REFRESH_ROTATION")
                .build());

        return issueTokens(user, user.getUsername(), null);
    }

    @Transactional
    public void logout(AuthenticatedUser currentUser, String accessToken) {
        Claims claims = jwtService.parseClaims(accessToken);
        revocationRepository.save(TokenRevocationEntity.builder()
                .tokenJti(claims.getId())
                .userId(currentUser.userId())
                .expiresAt(claims.getExpiration().toInstant())
                .reason("LOGOUT")
                .build());

        loginHistoryRepository.findByTokenJti(claims.getId()).ifPresent(lh -> {
            lh.setLogoutAt(Instant.now());
            loginHistoryRepository.save(lh);
        });
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_PASSWORD", "Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest req) {
        UserEntity user = userRepository.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account registered with that email"));
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        userRepository.save(user);
        return token;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        UserEntity user = userRepository.findByResetToken(req.token())
                .orElseThrow(() -> new BusinessException("INVALID_RESET_TOKEN", "Invalid or expired reset token"));
        if (user.getResetTokenExpiresAt() == null || user.getResetTokenExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("EXPIRED_RESET_TOKEN", "Reset token has expired");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);
    }

    private AuthResponse issueTokens(UserEntity user, String identifier, String ipAddress) {
        List<String> roles = user.getRoles().stream().map(RoleEntity::getName).toList();
        String access = jwtService.generateAccessToken(user.getId(), user.getUsername(), roles);
        String refresh = jwtService.generateRefreshToken(user.getId(), user.getUsername());
        String accessJti = jwtService.parseClaims(access).getId();

        loginHistoryRepository.save(LoginHistoryEntity.builder()
                .userId(user.getId())
                .identifierUsed(identifier)
                .tokenJti(accessJti)
                .loginStatus("SUCCESS")
                .ipAddress(ipAddress)
                .build());

        return new AuthResponse(access, refresh, user.getId(), user.getUsername(), user.getEmail(), roles);
    }
}
