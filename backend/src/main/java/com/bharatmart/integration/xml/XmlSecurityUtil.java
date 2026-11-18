package com.bharatmart.integration.xml;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.io.StringReader;

public final class XmlSecurityUtil {

    private XmlSecurityUtil() {}

    /**
     * Parses XML with strict XXE (XML External Entity) protection enabled.
     */
    public static Document parseSafe(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        return dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    public static void validateAgainstXsd(String xml, String xsdClasspathResource) throws Exception {
        parseSafe(xml); // Enforce XXE protection first
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        sf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        try (InputStream is = XmlSecurityUtil.class.getClassLoader().getResourceAsStream(xsdClasspathResource)) {
            if (is == null) {
                throw new IllegalStateException("XSD not found: " + xsdClasspathResource);
            }
            Schema schema = sf.newSchema(new StreamSource(is));
            schema.newValidator().validate(new StreamSource(new StringReader(xml)));
        }
    }

    public static String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public static String maskSensitiveData(String xml) {
        if (xml == null) return null;
        return xml.replaceAll("(?i)<(Password|Token|Secret)>.*?</\\1>", "<$1>***MASKED***</$1>");
    }
}
