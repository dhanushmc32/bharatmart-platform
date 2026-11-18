package com.bharatmart.integration;

import com.bharatmart.integration.xml.XmlSecurityUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XmlSecurityUtilTest {

    @Test
    void shouldRejectXxeDoctypeAttack() {
        String maliciousXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE foo [ <!ENTITY xxe SYSTEM "file:///etc/passwd"> ]>
                <BharatMartMessage xmlns="http://bharatmart.in/schema/integration/v1">
                    <Header><MessageId>&xxe;</MessageId></Header>
                </BharatMartMessage>
                """;
        assertThrows(Exception.class, () -> XmlSecurityUtil.parseSafe(maliciousXml));
    }

    @Test
    void shouldValidateValidOrderCreateXmlAgainstXsd() throws Exception {
        String validXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <BharatMartMessage xmlns="http://bharatmart.in/schema/integration/v1">
                    <Header>
                        <MessageId>msg-1234</MessageId>
                        <CorrelationId>corr-5678</CorrelationId>
                        <SourceSystem>ORDER_SERVICE</SourceSystem>
                        <DestinationSystem>INVENTORY_SERVICE</DestinationSystem>
                        <MessageType>ORDER_CREATE</MessageType>
                        <Timestamp>2026-11-18T14:00:00+05:30</Timestamp>
                    </Header>
                    <Payload>
                        <OrderCreate>
                            <OrderNumber>ORD-1001</OrderNumber>
                            <StoreCode>IN-KA-BLR-STORE001</StoreCode>
                            <PaymentMethod>COD</PaymentMethod>
                            <TotalAmount>1299.00</TotalAmount>
                        </OrderCreate>
                    </Payload>
                </BharatMartMessage>
                """;
        assertDoesNotThrow(() -> XmlSecurityUtil.validateAgainstXsd(validXml, "xsd/bharatmart-envelope.xsd"));
    }

    @Test
    void shouldMaskSensitiveXmlTags() {
        String raw = "<Data><Password>Secret123</Password><OrderNumber>ORD-1</OrderNumber></Data>";
        String masked = XmlSecurityUtil.maskSensitiveData(raw);
        assertFalse(masked.contains("Secret123"));
        assertTrue(masked.contains("***MASKED***"));
    }
}
