package com.bharatmart.payment.service;

import com.bharatmart.payment.entity.PaymentEntity;
import com.bharatmart.payment.entity.PaymentMethod;
import java.math.BigDecimal;

public interface PaymentProcessor {
    boolean supports(PaymentMethod method);
    PaymentEntity initializePayment(Long orderId, BigDecimal amount);
}
