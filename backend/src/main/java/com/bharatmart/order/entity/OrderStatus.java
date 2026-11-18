package com.bharatmart.order.entity;

public enum OrderStatus {
    PENDING_CONFIRMATION,
    CONFIRMED,
    PACKED,
    DISPATCHED,
    DELIVERED,
    CANCELLED,
    CANCELLED_OUT_OF_STOCK
}
