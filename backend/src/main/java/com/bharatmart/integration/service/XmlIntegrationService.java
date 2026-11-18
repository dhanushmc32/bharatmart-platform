package com.bharatmart.integration.service;

import com.bharatmart.common.exception.ResourceNotFoundException;
import com.bharatmart.integration.entity.MessageProcessingHistoryEntity;
import com.bharatmart.integration.entity.XmlMessageEntity;
import com.bharatmart.integration.repository.MessageProcessingHistoryRepository;
import com.bharatmart.integration.repository.XmlMessageRepository;
import com.bharatmart.integration.xml.XmlSecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class XmlIntegrationService {

    private final XmlMessageRepository xmlMessageRepository;
    private final MessageProcessingHistoryRepository historyRepository;
    private final ProducerTemplate producerTemplate;

    @Transactional
    public XmlMessageEntity dispatchOrderCreateXml(Long orderId, String orderNumber, String storeCode,
                                                     String correlationId, BigDecimal totalAmount) {
        String payload = """
                <OrderCreate>
                    <OrderNumber>%s</OrderNumber>
                    <StoreCode>%s</StoreCode>
                    <PaymentMethod>COD</PaymentMethod>
                    <TotalAmount>%s</TotalAmount>
                </OrderCreate>
                """.formatted(XmlSecurityUtil.escapeXml(orderNumber), XmlSecurityUtil.escapeXml(storeCode), totalAmount);

        return sendEnvelope(orderId, orderNumber, storeCode, correlationId,
                "ORDER_SERVICE", "INVENTORY_SERVICE", "ORDER_CREATE", payload);
    }

    @Transactional
    public XmlMessageEntity dispatchOrderCancelXml(Long orderId, String orderNumber, String storeCode,
                                                     String correlationId, String reason) {
        String payload = """
                <OrderCancel>
                    <OrderNumber>%s</OrderNumber>
                    <StoreCode>%s</StoreCode>
                    <Reason>%s</Reason>
                </OrderCancel>
                """.formatted(XmlSecurityUtil.escapeXml(orderNumber), XmlSecurityUtil.escapeXml(storeCode),
                XmlSecurityUtil.escapeXml(reason));

        return sendEnvelope(orderId, orderNumber, storeCode, correlationId,
                "ORDER_SERVICE", "INVENTORY_SERVICE", "ORDER_CANCEL", payload);
    }

    @Transactional
    public XmlMessageEntity dispatchCodConfirmXml(Long orderId, String orderNumber, String storeCode,
                                                    String correlationId, BigDecimal amount, String paymentStatus) {
        String payload = """
                <CodPaymentConfirm>
                    <OrderNumber>%s</OrderNumber>
                    <Amount>%s</Amount>
                    <PaymentStatus>%s</PaymentStatus>
                </CodPaymentConfirm>
                """.formatted(XmlSecurityUtil.escapeXml(orderNumber), amount, XmlSecurityUtil.escapeXml(paymentStatus));

        return sendEnvelope(orderId, orderNumber, storeCode, correlationId,
                "PAYMENT_SERVICE", "ORDER_SERVICE", "COD_PAYMENT_CONFIRM", payload);
    }

    @Transactional
    public XmlMessageEntity retryMessage(Long xmlMessageId) {
        XmlMessageEntity msg = xmlMessageRepository.findById(xmlMessageId)
                .orElseThrow(() -> new ResourceNotFoundException("XML Message not found: " + xmlMessageId));

        msg.setRetryCount(msg.getRetryCount() + 1);
        msg.setStatus("RETRYING");
        xmlMessageRepository.save(msg);
        logHistory(msg.getId(), "RETRYING", msg.getRetryCount(), "Manual admin retry initiated");

        return executeCamelRoute(msg, "", "");
    }

    @Transactional(readOnly = true)
    public List<XmlMessageEntity> searchMessages(String status, Long orderId, String query) {
        String cleanStatus = (status != null && !status.isBlank()) ? status : null;
        String cleanQuery = (query != null && !query.isBlank()) ? query.trim() : null;
        return xmlMessageRepository.searchMessages(cleanStatus, orderId, cleanQuery);
    }

    @Transactional(readOnly = true)
    public List<MessageProcessingHistoryEntity> getProcessingHistory(Long xmlMessageId) {
        return historyRepository.findByXmlMessageIdOrderByLoggedAtAsc(xmlMessageId);
    }

    private XmlMessageEntity sendEnvelope(Long orderId, String orderNumber, String storeCode, String correlationId,
                                          String src, String dst, String type, String innerPayload) {
        String messageId = "msg-" + UUID.randomUUID();
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <BharatMartMessage xmlns="http://bharatmart.in/schema/integration/v1">
                    <Header>
                        <MessageId>%s</MessageId>
                        <CorrelationId>%s</CorrelationId>
                        <SourceSystem>%s</SourceSystem>
                        <DestinationSystem>%s</DestinationSystem>
                        <MessageType>%s</MessageType>
                        <Timestamp>%s</Timestamp>
                    </Header>
                    <Payload>%s</Payload>
                </BharatMartMessage>
                """.formatted(messageId, XmlSecurityUtil.escapeXml(correlationId), src, dst, type,
                OffsetDateTime.now(), innerPayload);

        XmlMessageEntity entity = xmlMessageRepository.save(XmlMessageEntity.builder()
                .messageId(messageId)
                .correlationId(correlationId)
                .orderId(orderId)
                .sourceSystem(src)
                .destinationSystem(dst)
                .messageType(type)
                .status("CREATED")
                .retryCount(0)
                .requestXml(XmlSecurityUtil.maskSensitiveData(xml))
                .build());
        logHistory(entity.getId(), "CREATED", 1, "XML envelope generated");

        return executeCamelRoute(entity, orderNumber, storeCode);
    }

    private XmlMessageEntity executeCamelRoute(XmlMessageEntity entity, String orderNumber, String storeCode) {
        entity.setStatus("PROCESSING");
        xmlMessageRepository.save(entity);
        logHistory(entity.getId(), "PROCESSING", entity.getRetryCount() + 1, "Dispatched to Apache Camel");

        try {
            Exchange result = producerTemplate.request("direct:order-outbound", ex -> {
                ex.getIn().setBody(entity.getRequestXml());
                ex.getIn().setHeader("messageId", entity.getMessageId());
                ex.getIn().setHeader("correlationId", entity.getCorrelationId());
                ex.getIn().setHeader("messageType", entity.getMessageType());
                ex.getIn().setHeader("orderNumber", orderNumber);
                ex.getIn().setHeader("storeCode", storeCode);
            });

            String dlqErr = result.getIn().getHeader("dlqError", String.class);
            if (dlqErr != null) {
                entity.setStatus("DEAD_LETTER");
                entity.setErrorDetails(dlqErr);
                xmlMessageRepository.save(entity);
                logHistory(entity.getId(), "DEAD_LETTER", entity.getRetryCount() + 1, dlqErr);
            } else {
                entity.setStatus("SUCCESS");
                entity.setResponseXml(XmlSecurityUtil.maskSensitiveData(result.getIn().getBody(String.class)));
                entity.setErrorDetails(null);
                xmlMessageRepository.save(entity);
                logHistory(entity.getId(), "SUCCESS", entity.getRetryCount() + 1, "XSD validated & acknowledged");
            }
        } catch (Exception ex) {
            entity.setStatus("FAILED");
            entity.setErrorDetails(ex.getMessage());
            xmlMessageRepository.save(entity);
            logHistory(entity.getId(), "FAILED", entity.getRetryCount() + 1, ex.getMessage());
        }
        return entity;
    }

    private void logHistory(Long xmlMsgId, String status, int attempt, String notes) {
        historyRepository.save(MessageProcessingHistoryEntity.builder()
                .xmlMessageId(xmlMsgId)
                .status(status)
                .attemptNumber(attempt)
                .notes(notes)
                .build());
    }
}
