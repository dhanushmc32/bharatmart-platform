package com.bharatmart.integration.repository;

import com.bharatmart.integration.entity.MessageProcessingHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageProcessingHistoryRepository extends JpaRepository<MessageProcessingHistoryEntity, Long> {
    List<MessageProcessingHistoryEntity> findByXmlMessageIdOrderByLoggedAtAsc(Long xmlMessageId);
}
