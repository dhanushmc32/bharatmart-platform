package com.bharatmart.integration.repository;

import com.bharatmart.integration.entity.XmlMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface XmlMessageRepository extends JpaRepository<XmlMessageEntity, Long> {
    Optional<XmlMessageEntity> findByMessageId(String messageId);

    @Query("""
        SELECT m FROM XmlMessageEntity m
        WHERE (:status IS NULL OR m.status = :status)
          AND (:orderId IS NULL OR m.orderId = :orderId)
          AND (:q IS NULL OR m.messageId LIKE CONCAT('%', :q, '%') OR m.correlationId LIKE CONCAT('%', :q, '%'))
        ORDER BY m.createdAt DESC
    """)
    List<XmlMessageEntity> searchMessages(
            @Param("status") String status,
            @Param("orderId") Long orderId,
            @Param("q") String q);
}
