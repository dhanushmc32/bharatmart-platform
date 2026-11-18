package com.bharatmart.payment.service;

import com.bharatmart.common.exception.BusinessException;
import com.bharatmart.common.exception.ResourceNotFoundException;
import com.bharatmart.integration.service.XmlIntegrationService;
import com.bharatmart.order.entity.OrderEntity;
import com.bharatmart.order.repository.OrderRepository;
import com.bharatmart.payment.entity.PaymentEntity;
import com.bharatmart.payment.entity.PaymentStatus;
import com.bharatmart.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CodPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final XmlIntegrationService xmlIntegrationService;

    public record UpdateCodStatusRequest(PaymentStatus status, String collectedBy, String remarks) {}

    @Transactional(readOnly = true)
    public List<PaymentEntity> listAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional
    public PaymentEntity updateCodStatus(Long orderId, UpdateCodStatusRequest req) {
        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for orderId: " + orderId));

        if (payment.getStatus() == PaymentStatus.PAID || payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException("INVALID_PAYMENT_STATE", "Payment already finalized in status: " + payment.getStatus());
        }

        payment.setStatus(req.status());
        if (req.collectedBy() != null) payment.setCollectedBy(req.collectedBy());
        if (req.remarks() != null) payment.setRemarks(req.remarks());

        if (req.status() == PaymentStatus.CASH_COLLECTED) {
            payment.setCollectedAt(Instant.now());
        } else if (req.status() == PaymentStatus.PAID) {
            if (payment.getCollectedAt() == null) payment.setCollectedAt(Instant.now());
            payment.setSettledAt(Instant.now());
        }

        PaymentEntity saved = paymentRepository.save(payment);

        if (req.status() == PaymentStatus.PAID) {
            OrderEntity order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
            xmlIntegrationService.dispatchCodConfirmXml(
                    order.getId(), order.getOrderNumber(), order.getStore().getStoreCode(),
                    order.getCorrelationId(), saved.getAmount(), saved.getStatus().name());
        }
        return saved;
    }
}
