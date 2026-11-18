package com.bharatmart.integration;

import com.bharatmart.integration.camel.BharatMartCamelRoutes;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BharatMartCamelRouteTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new BharatMartCamelRoutes();
    }

    @Test
    void shouldRouteValidOrderCreateXmlAndReturnInventoryConfirmAck() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <BharatMartMessage xmlns="http://bharatmart.in/schema/integration/v1">
                    <Header>
                        <MessageId>msg-test-1</MessageId>
                        <CorrelationId>corr-test-1</CorrelationId>
                        <SourceSystem>ORDER_SERVICE</SourceSystem>
                        <DestinationSystem>INVENTORY_SERVICE</DestinationSystem>
                        <MessageType>ORDER_CREATE</MessageType>
                        <Timestamp>2026-11-18T14:00:00+05:30</Timestamp>
                    </Header>
                    <Payload>
                        <OrderCreate>
                            <OrderNumber>ORD-9001</OrderNumber>
                            <StoreCode>IN-KA-BLR-STORE001</StoreCode>
                            <PaymentMethod>COD</PaymentMethod>
                            <TotalAmount>1299.00</TotalAmount>
                        </OrderCreate>
                    </Payload>
                </BharatMartMessage>
                """;

        Exchange out = template.request("direct:order-outbound", ex -> {
            ex.getIn().setBody(xml);
            ex.getIn().setHeader("messageId", "msg-test-1");
            ex.getIn().setHeader("correlationId", "corr-test-1");
            ex.getIn().setHeader("messageType", "ORDER_CREATE");
            ex.getIn().setHeader("orderNumber", "ORD-9001");
            ex.getIn().setHeader("storeCode", "IN-KA-BLR-STORE001");
        });

        String ack = out.getIn().getBody(String.class);
        assertNotNull(ack);
        assertTrue(ack.contains("<MessageType>INVENTORY_CONFIRM</MessageType>"));
        assertTrue(ack.contains("<ReservationStatus>RESERVED</ReservationStatus>"));
    }
}
