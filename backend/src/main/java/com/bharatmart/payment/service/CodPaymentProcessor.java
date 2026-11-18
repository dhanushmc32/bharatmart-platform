package com.bharatmart.payment.service;

import com.bharatmart.payment.entity.PaymentEntity;
import com.bharatmart.payment.entity.PaymentMethod;
import com.bharatmart.payment.entity.PaymentStatus;
import com.bharatmart.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CodPaymentProcessor implements PaymentProcessor {

    private final PaymentRepository paymentRepository;

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.COD;
    }

    @Override
    @Transactional
    public PaymentEntity initializePayment(Long orderId, BigDecimal amount) {
        return paymentRepository.save(PaymentEntity.builder()
                .orderId(orderId)
                .paymentMethod(PaymentMethod.COD)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .remarks("Cash on Delivery selected at checkout")
                .build());
    }
}
