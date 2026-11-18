package com.bharatmart.order;

import com.bharatmart.cart.entity.CartItemEntity;
import com.bharatmart.cart.entity.ShoppingCartEntity;
import com.bharatmart.cart.repository.ShoppingCartRepository;
import com.bharatmart.common.exception.BusinessException;
import com.bharatmart.integration.service.XmlIntegrationService;
import com.bharatmart.inventory.service.InventoryService;
import com.bharatmart.order.dto.OrderResponse;
import com.bharatmart.order.dto.PlaceOrderRequest;
import com.bharatmart.order.entity.OrderEntity;
import com.bharatmart.order.entity.OrderStatus;
import com.bharatmart.order.repository.OrderRepository;
import com.bharatmart.order.service.OrderService;
import com.bharatmart.payment.entity.PaymentEntity;
import com.bharatmart.payment.entity.PaymentMethod;
import com.bharatmart.payment.entity.PaymentStatus;
import com.bharatmart.payment.repository.PaymentRepository;
import com.bharatmart.payment.service.CodPaymentProcessor;
import com.bharatmart.product.entity.ProductEntity;
import com.bharatmart.product.entity.SkuEntity;
import com.bharatmart.store.entity.StoreEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ShoppingCartRepository cartRepository;
    @Mock private InventoryService inventoryService;
    @Mock private PaymentRepository paymentRepository;
    @Mock private XmlIntegrationService xmlIntegrationService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldPlaceCodOrderAndReserveStock() {
        var codProcessor = new CodPaymentProcessor(paymentRepository);
        var orderService = new OrderService(orderRepository, cartRepository, inventoryService,
                paymentRepository, List.of(codProcessor), xmlIntegrationService, eventPublisher);

        StoreEntity store = StoreEntity.builder().id(1L).storeCode("IN-KA-BLR-STORE001").build();
        ProductEntity product = ProductEntity.builder().id(1L).name("boAt Airdopes 141").build();
        SkuEntity sku = SkuEntity.builder().id(1L).product(product).skuCode("SKU-ELEC-1001")
                .sellingPrice(new BigDecimal("1299.00")).build();

        ShoppingCartEntity cart = ShoppingCartEntity.builder().id(10L).userId(3L).store(store).build();
        cart.getItems().add(CartItemEntity.builder().id(100L).cart(cart).sku(sku).quantity(2).build());

        when(cartRepository.findByUserIdAndStoreStoreCode(3L, "IN-KA-BLR-STORE001")).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            OrderEntity o = inv.getArgument(0);
            o.setId(55L);
            return o;
        });
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse res = orderService.placeOrder(3L,
                new PlaceOrderRequest("IN-KA-BLR-STORE001", "Indiranagar, Bengaluru 560038", PaymentMethod.COD, null));

        assertEquals(OrderStatus.CONFIRMED, res.status());
        assertEquals(PaymentMethod.COD, res.paymentMethod());
        assertEquals(PaymentStatus.PENDING, res.paymentStatus());
        assertEquals(new BigDecimal("2598.00"), res.totalAmount());
        verify(inventoryService).reserveStock(eq(1L), eq(1L), eq(2), anyString());
        verify(xmlIntegrationService).dispatchOrderCreateXml(eq(55L), anyString(), eq("IN-KA-BLR-STORE001"), anyString(), eq(new BigDecimal("2598.00")));
    }

    @Test
    void shouldRejectEmptyCartCheckout() {
        var orderService = new OrderService(orderRepository, cartRepository, inventoryService,
                paymentRepository, List.of(), xmlIntegrationService, eventPublisher);
        when(cartRepository.findByUserIdAndStoreStoreCode(3L, "IN-KA-BLR-STORE001")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> orderService.placeOrder(3L,
                new PlaceOrderRequest("IN-KA-BLR-STORE001", "Bengaluru", PaymentMethod.COD, null)));
    }
}
