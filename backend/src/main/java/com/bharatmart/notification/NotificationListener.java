package com.bharatmart.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationListener {

    public record OrderNotificationEvent(String orderNumber, String storeCode, String eventType, String summary) {}

    @EventListener
    public void onOrderEvent(OrderNotificationEvent event) {
        log.info("[NOTIFICATION] Order={} Store={} Event={} Detail={}",
                event.orderNumber(), event.storeCode(), event.eventType(), event.summary());
    }
}
