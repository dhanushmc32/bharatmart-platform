package com.bharatmart.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "login_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginHistoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "identifier_used", nullable = false, length = 120)
    private String identifierUsed;

    @Column(name = "login_at", nullable = false, updatable = false)
    private Instant loginAt = Instant.now();

    @Column(name = "logout_at")
    private Instant logoutAt;

    @Column(name = "token_jti", length = 64)
    private String tokenJti;

    @Column(name = "login_status", nullable = false, length = 20)
    private String loginStatus;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "failure_reason", length = 200)
    private String failureReason;
}
