package com.bharatmart.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "inventory_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryHistoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private InventoryEntity inventory;

    @Column(name = "change_qty", nullable = false)
    private int changeQty;

    @Column(name = "previous_available", nullable = false)
    private int previousAvailable;

    @Column(name = "new_available", nullable = false)
    private int newAvailable;

    @Column(nullable = false, length = 50)
    private String reason;

    @Column(name = "reference_id", length = 64)
    private String referenceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
