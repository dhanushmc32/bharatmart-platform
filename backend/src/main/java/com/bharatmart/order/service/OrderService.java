package com.bharatmart.order.service;

import com.bharatmart.cart.entity.CartItemEntity;
import com.bharatmart.cart.entity.ShoppingCartEntity;
import com.bharatmart.cart.repository.ShoppingCartRepository;
import com.bharatmart.common.exception.BusinessException;
import com.bharatmart.common.exception.ResourceNotFoundException;
import com.bharatmart.integration.service.XmlIntegrationService;
import com.bharatmart.inventory.service.InventoryService;
import com.bharatmart.notification.NotificationListener.OrderNotificationEvent;
import com.bharatmart.order.dto.OrderResponse;
import com.bharatmart.order.dto.PlaceOrderRequest;
import com.bharatmart.order.entity.OrderEntity;
import com.bharatmart.order.entity.OrderItemEntity;
import com.bharatmart.order.entity.OrderStatus;
import com.bharatmart.order.repository.OrderRepository;
import com.bharatmart.payment.entity.PaymentEntity;
import com.bharatmart.payment.entity.PaymentMethod;
import com.bharatmart.payment.entity.PaymentStatus;
import com.bharatmart.payment.repository.PaymentRepository;
import com.bharatmart.payment.service.PaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ShoppingCartRepository cartRepository;
    private final InventoryService inventoryService;
    private final PaymentRepository paymentRepository;
    private final List<PaymentProcessor> paymentProcessors;
    private final XmlIntegrationService xmlIntegrationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest req) {
        if (req.paymentMethod() != PaymentMethod.COD) {
            throw new BusinessException("PAYMENT_NOT_SUPPORTED", "Only Cash on Delivery (COD) is supported");
        }

        ShoppingCartEntity cart = cartRepository.findByUserIdAndStoreStoreCode(userId, req.storeCode())
                .orElseThrow(() -> new BusinessException("EMPTY_CART", "Shopping cart is empty for store " + req.storeCode()));

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("EMPTY_CART", "Cannot place order with an empty cart");
        }

        String orderNumber = "ORD-" + System.currentTimeMillis();
        String correlationId = "corr-" + UUID.randomUUID();

        OrderEntity order = OrderEntity.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .store(cart.getStore())
                .shippingAddressSnapshot(req.shippingAddress())
                .status(OrderStatus.CONFIRMED)
                .correlationId(correlationId)
                .notes(req.notes())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (CartItemEntity ci : cart.getItems()) {
            inventoryService.reserveStock(cart.getStore().getId(), ci.getSku().getId(), ci.getQuantity(), orderNumber);
            BigDecimal lineTotal = ci.getSku().getSellingPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(lineTotal);

            order.getItems().add(OrderItemEntity.builder()
                    .order(order)
                    .sku(ci.getSku())
                    .skuCodeSnapshot(ci.getSku().getSkuCode())
                    .productNameSnapshot(ci.getSku().getProduct().getName())
                    .quantity(ci.getQuantity())
                    .unitPrice(ci.getSku().getSellingPrice())
                    .lineTotal(lineTotal)
                    .build());
        }
        order.setTotalAmount(total);
        OrderEntity savedOrder = orderRepository.save(order);

        PaymentProcessor processor = paymentProcessors.stream()
                .filter(p -> p.supports(req.paymentMethod()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_SUPPORTED", "COD processor unavailable"));
        PaymentEntity payment = processor.initializePayment(savedOrder.getId(), total);

        cart.getItems().clear();
        cartRepository.save(cart);

        // Emit XML message via Apache Camel & publish notification event
        xmlIntegrationService.dispatchOrderCreateXml(
                savedOrder.getId(), orderNumber, savedOrder.getStore().getStoreCode(), correlationId, total);
        eventPublisher.publishEvent(new OrderNotificationEvent(
                orderNumber, savedOrder.getStore().getStoreCode(), "ORDER_CONFIRMED", "COD order placed"));

        return toResponse(savedOrder, payment);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderNumber) {
        OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
        PaymentEntity payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        return toResponse(order, payment);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(o -> toResponse(o, paymentRepository.findByOrderId(o.getId()).orElse(null)))
                .toList();
    }

    @Transactional
    public OrderResponse cancelOrder(String orderNumber) {
        OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("INVALID_ORDER_STATE", "Order cannot be cancelled in status: " + order.getStatus());
        }

        for (OrderItemEntity item : order.getItems()) {
            inventoryService.releaseStock(order.getStore().getId(), item.getSku().getId(), item.getQuantity(), orderNumber);
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (payment != null) {
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
        }

        xmlIntegrationService.dispatchOrderCancelXml(
                order.getId(), orderNumber, order.getStore().getStoreCode(),
                order.getCorrelationId(), "CUSTOMER_CANCELLED");

        return toResponse(order, payment);
    }

    private OrderResponse toResponse(OrderEntity o, PaymentEntity p) {
        List<OrderResponse.OrderItemView> itemViews = o.getItems().stream()
                .map(i -> new OrderResponse.OrderItemView(
                        i.getSkuCodeSnapshot(), i.getProductNameSnapshot(),
                        i.getQuantity(), i.getUnitPrice(), i.getLineTotal()))
                .toList();

        return new OrderResponse(
                o.getId(), o.getOrderNumber(), o.getStore().getStoreCode(), o.getStatus(),
                p != null ? p.getPaymentMethod() : PaymentMethod.COD,
                p != null ? p.getStatus() : PaymentStatus.PENDING,
                o.getTotalAmount(), o.getShippingAddressSnapshot(), o.getCorrelationId(),
                o.getCreatedAt(), itemViews
        );
    }
}
