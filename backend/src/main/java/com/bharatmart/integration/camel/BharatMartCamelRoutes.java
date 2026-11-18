package com.bharatmart.integration.camel;

import com.bharatmart.integration.xml.XmlSecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
public class BharatMartCamelRoutes extends RouteBuilder {

    @Override
    public void configure() {
        // Dead Letter Channel & Retry Policy (3 retries with exponential backoff)
        errorHandler(deadLetterChannel("direct:dead-letter-channel")
                .maximumRedeliveries(3)
                .redeliveryDelay(500)
                .backOffMultiplier(2.0)
                .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN)
                .onRedelivery(exchange -> {
                    String msgId = exchange.getIn().getHeader("messageId", String.class);
                    Integer count = exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER, Integer.class);
                    log.warn("Retrying XML messageId={} attempt={}", msgId, count);
                }));

        from("direct:order-outbound")
                .routeId("bharatmart-xml-outbound")
                .process(exchange -> {
                    String xml = exchange.getIn().getBody(String.class);
                    // Enforce XXE-safe parsing & XSD schema validation
                    XmlSecurityUtil.validateAgainstXsd(xml, "xsd/bharatmart-envelope.xsd");
                })
                .choice()
                    .when(header("messageType").isEqualTo("ORDER_CREATE"))
                        .to("direct:inventory-reserve-handler")
                    .when(header("messageType").isEqualTo("ORDER_CANCEL"))
                        .to("direct:inventory-release-handler")
                    .when(header("messageType").isEqualTo("COD_PAYMENT_CONFIRM"))
                        .to("direct:cod-confirm-handler")
                    .otherwise()
                        .to("direct:generic-ack-handler")
                .end();

        from("direct:inventory-reserve-handler")
                .routeId("inventory-reserve-ack")
                .process(exchange -> {
                    String corrId = exchange.getIn().getHeader("correlationId", String.class);
                    String orderNumber = exchange.getIn().getHeader("orderNumber", String.class);
                    String storeCode = exchange.getIn().getHeader("storeCode", String.class);
                    String ackXml = buildAckXml(corrId, "INVENTORY_SERVICE", "ORDER_SERVICE",
                            "INVENTORY_CONFIRM",
                            "<InventoryConfirm><OrderNumber>" + XmlSecurityUtil.escapeXml(orderNumber) +
                                    "</OrderNumber><StoreCode>" + XmlSecurityUtil.escapeXml(storeCode) +
                                    "</StoreCode><ReservationStatus>RESERVED</ReservationStatus></InventoryConfirm>");
                    exchange.getIn().setBody(ackXml);
                });

        from("direct:inventory-release-handler")
                .routeId("inventory-release-ack")
                .process(exchange -> {
                    String corrId = exchange.getIn().getHeader("correlationId", String.class);
                    String orderNumber = exchange.getIn().getHeader("orderNumber", String.class);
                    String storeCode = exchange.getIn().getHeader("storeCode", String.class);
                    String ackXml = buildAckXml(corrId, "INVENTORY_SERVICE", "ORDER_SERVICE",
                            "INVENTORY_CONFIRM",
                            "<InventoryConfirm><OrderNumber>" + XmlSecurityUtil.escapeXml(orderNumber) +
                                    "</OrderNumber><StoreCode>" + XmlSecurityUtil.escapeXml(storeCode) +
                                    "</StoreCode><ReservationStatus>RELEASED</ReservationStatus></InventoryConfirm>");
                    exchange.getIn().setBody(ackXml);
                });

        from("direct:cod-confirm-handler")
                .routeId("cod-confirm-ack")
                .process(exchange -> {
                    String corrId = exchange.getIn().getHeader("correlationId", String.class);
                    String orderNumber = exchange.getIn().getHeader("orderNumber", String.class);
                    String ackXml = buildAckXml(corrId, "PAYMENT_SERVICE", "ORDER_SERVICE",
                            "COD_PAYMENT_CONFIRM",
                            "<CodPaymentConfirm><OrderNumber>" + XmlSecurityUtil.escapeXml(orderNumber) +
                                    "</OrderNumber><Amount>0.00</Amount><PaymentStatus>ACKNOWLEDGED</PaymentStatus></CodPaymentConfirm>");
                    exchange.getIn().setBody(ackXml);
                });

        from("direct:generic-ack-handler")
                .routeId("generic-ack")
                .setBody(simple("${body}"));

        from("direct:dead-letter-channel")
                .routeId("bharatmart-dlq")
                .process(exchange -> {
                    Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    String msgId = exchange.getIn().getHeader("messageId", String.class);
                    log.error("XML message moved to DEAD_LETTER messageId={} cause={}",
                            msgId, cause != null ? cause.getMessage() : "Unknown");
                    exchange.getIn().setHeader("dlqError", cause != null ? cause.getMessage() : "Unknown error");
                });
    }

    private String buildAckXml(String corrId, String src, String dst, String type, String innerPayload) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <BharatMartMessage xmlns="http://bharatmart.in/schema/integration/v1">
                    <Header>
                        <MessageId>msg-%s</MessageId>
                        <CorrelationId>%s</CorrelationId>
                        <SourceSystem>%s</SourceSystem>
                        <DestinationSystem>%s</DestinationSystem>
                        <MessageType>%s</MessageType>
                        <Timestamp>%s</Timestamp>
                    </Header>
                    <Payload>%s</Payload>
                </BharatMartMessage>
                """.formatted(
                UUID.randomUUID(),
                XmlSecurityUtil.escapeXml(corrId),
                src, dst, type,
                OffsetDateTime.now(),
                innerPayload
        );
    }
}
