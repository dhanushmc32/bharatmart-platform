package com.bharatmart.integration.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "message_processing_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageProcessingHistoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "xml_message_id", nullable = false)
    private Long xmlMessageId;

    @Column(nullable = false, length = 25)
    private String status;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber = 1;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "logged_at", nullable = false, updatable = false)
    private Instant loggedAt = Instant.now();
}
