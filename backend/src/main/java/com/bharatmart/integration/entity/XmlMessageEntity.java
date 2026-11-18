package com.bharatmart.integration.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "xml_messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class XmlMessageEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, unique = true, length = 64)
    private String messageId;

    @Column(name = "correlation_id", nullable = false, length = 64)
    private String correlationId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "source_system", nullable = false, length = 50)
    private String sourceSystem;

    @Column(name = "destination_system", nullable = false, length = 50)
    private String destinationSystem;

    @Column(name = "message_type", nullable = false, length = 40)
    private String messageType;

    @Column(nullable = false, length = 25)
    private String status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "request_xml", nullable = false, columnDefinition = "LONGTEXT")
    private String requestXml;

    @Column(name = "response_xml", columnDefinition = "LONGTEXT")
    private String responseXml;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }
}
