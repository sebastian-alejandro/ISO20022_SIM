package com.kuvasz.iso20022.simulator.core.parser;

import com.kuvasz.iso20022.simulator.exception.ParsingException;
import com.kuvasz.iso20022.simulator.model.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

/**
 * Parser genérico para mensajes ISO 20022 usando DOM
 */
@Component
public class GenericXMLParser implements MessageParser {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericXMLParser.class);
    
    private final DocumentBuilderFactory documentBuilderFactory;
    private final XPathFactory xPathFactory;
    
    // Namespaces comunes de ISO 20022
    private static final String NAMESPACE_PAIN_001 = "urn:iso:std:iso:20022:tech:xsd:pain.001.001.03";
    private static final String NAMESPACE_PACS_008 = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02";
    private static final String NAMESPACE_CAMT_053 = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.02";
    
    public GenericXMLParser() {
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.documentBuilderFactory.setNamespaceAware(true);
        this.documentBuilderFactory.setValidating(false);
        
        // Configuración de seguridad para prevenir XXE attacks
        try {
            this.documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            this.documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            this.documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            this.documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {
            logger.warn("Could not configure XML security features: {}", e.getMessage());
        }
        
        this.xPathFactory = XPathFactory.newInstance();
    }
    
    @Override
    public MessageContext parse(String xmlContent) throws ParsingException {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw new ParsingException("XML content is null or empty");
        }
        
        try {
            logger.debug("Starting XML parsing for message of length: {}", xmlContent.length());
            long startTime = System.currentTimeMillis();
            
            Document document = parseXMLDocument(xmlContent);
            MessageContext context = extractMessageContext(document, xmlContent);
            
            long parseTime = System.currentTimeMillis() - startTime;
            logger.debug("XML parsing completed in {}ms for message: {}", parseTime, context.getMessageId());
            
            return context;
            
        } catch (Exception e) {
            logger.error("Error parsing XML message", e);
            throw new ParsingException("Failed to parse XML message: " + e.getMessage(), e);
        }
    }
    
    private Document parseXMLDocument(String xmlContent) throws ParsingException {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
            return documentBuilder.parse(inputStream);
            
        } catch (ParserConfigurationException e) {
            throw new ParsingException("Parser configuration error: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new ParsingException("XML parsing error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ParsingException("I/O error while parsing XML: " + e.getMessage(), e);
        }
    }
    
    private MessageContext extractMessageContext(Document document, String originalXml) throws ParsingException {
        Element rootElement = document.getDocumentElement();
        
        MessageContext context = new MessageContext();
        context.setOriginalXml(originalXml);
        
        try {
            // Extraer información básica del mensaje
            extractBasicMessageInfo(rootElement, context);
            
            // Extraer identificadores de negocio
            extractBusinessIdentifiers(document, context);
            
            // Extraer información de participantes
            extractParticipants(document, context);
            
            // Determinar el tipo de mensaje basado en el namespace y elemento raíz
            determineMessageType(rootElement, context);
            
            return context;
            
        } catch (Exception e) {
            logger.error("Error extracting message context", e);
            throw new ParsingException("Failed to extract message context: " + e.getMessage(), e);
        }
    }
    
    private void extractBasicMessageInfo(Element rootElement, MessageContext context) {
        // Generar un ID único si no se encuentra uno
        String messageId = UUID.randomUUID().toString();
        context.setMessageId(messageId);
        
        // Extraer timestamp de creación si está disponible
        try {
            String creationTime = getTextContentByTagName(rootElement, "CreDtTm");
            if (creationTime != null && !creationTime.isEmpty()) {
                LocalDateTime creationDateTime = parseISO20022DateTime(creationTime);
                context.setCreationDateTime(creationDateTime);
            }
        } catch (Exception e) {
            logger.debug("Could not parse creation date time: {}", e.getMessage());
        }
    }
    
    private void extractBusinessIdentifiers(Document document, MessageContext context) {
        try {
            XPath xpath = xPathFactory.newXPath();
            
            // Intentar extraer Message Identification
            String msgId = extractTextByXPath(document, xpath, "//MsgId | //MessageIdentification");
            if (msgId != null) {
                context.setBusinessMessageIdentifier(msgId);
            }
            
            // Intentar extraer End to End Identification
            String endToEndId = extractTextByXPath(document, xpath, "//EndToEndId");
            if (endToEndId != null) {
                context.addProperty("endToEndId", endToEndId);
            }
            
            // Intentar extraer Instruction Identification
            String instrId = extractTextByXPath(document, xpath, "//InstrId");
            if (instrId != null) {
                context.addProperty("instructionId", instrId);
            }
            
        } catch (Exception e) {
            logger.debug("Could not extract business identifiers: {}", e.getMessage());
        }
    }
    
    private void extractParticipants(Document document, MessageContext context) {
        try {
            XPath xpath = xPathFactory.newXPath();
            
            // Intentar extraer información del iniciador/deudor
            String debtorName = extractTextByXPath(document, xpath, "//Dbtr/Nm | //InitgPty/Nm");
            if (debtorName != null) {
                context.setSenderId(debtorName);
            }
            
            // Intentar extraer información del acreedor
            String creditorName = extractTextByXPath(document, xpath, "//Cdtr/Nm");
            if (creditorName != null) {
                context.setReceiverId(creditorName);
            }
            
        } catch (Exception e) {
            logger.debug("Could not extract participant information: {}", e.getMessage());
        }
    }
    
    private void determineMessageType(Element rootElement, MessageContext context) {
        String localName = rootElement.getLocalName();
        String namespaceURI = rootElement.getNamespaceURI();
        
        String messageType = "UNKNOWN";
        
        if (namespaceURI != null) {
            if (namespaceURI.contains("pain.001")) {
                messageType = "PAIN.001"; // Customer Credit Transfer Initiation
            } else if (namespaceURI.contains("pacs.008")) {
                messageType = "PACS.008"; // Financial Institution To Financial Institution Customer Credit Transfer
            } else if (namespaceURI.contains("camt.053")) {
                messageType = "CAMT.053"; // Bank To Customer Statement
            } else if (localName != null) {
                // Fallback basado en el nombre del elemento raíz
                if (localName.contains("CstmrCdtTrfInitn")) {
                    messageType = "PAIN.001";
                } else if (localName.contains("FIToFICstmrCdtTrf")) {
                    messageType = "PACS.008";
                } else if (localName.contains("BkToCstmrStmt")) {
                    messageType = "CAMT.053";
                }
            }
        }
        
        context.setMessageType(messageType);
        context.setMessageDefinitionIdentifier(namespaceURI);
        
        logger.debug("Determined message type: {} for namespace: {}", messageType, namespaceURI);
    }
    
    private String getTextContentByTagName(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return node.getTextContent();
        }
        return null;
    }
    
    private String extractTextByXPath(Document document, XPath xpath, String expression) {
        try {
            return (String) xpath.evaluate(expression, document, XPathConstants.STRING);
        } catch (Exception e) {
            logger.debug("Could not evaluate XPath expression '{}': {}", expression, e.getMessage());
            return null;
        }
    }
    
    private LocalDateTime parseISO20022DateTime(String dateTimeStr) throws DateTimeParseException {
        // ISO 20022 usa formato ISO 8601: 2023-12-25T10:30:00Z o 2023-12-25T10:30:00+02:00
        try {
            // Intentar diferentes formatos comunes
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ISO_DATE_TIME,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            };
            
            String cleanDateTime = dateTimeStr.replaceAll("Z$", "").replaceAll("[+-]\\d{2}:?\\d{2}$", "");
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDateTime.parse(cleanDateTime, formatter);
                } catch (DateTimeParseException ignored) {
                    // Intentar el siguiente formato
                }
            }
            
            throw new DateTimeParseException("Unable to parse date time", dateTimeStr, 0);
            
        } catch (Exception e) {
            throw new DateTimeParseException("Error parsing date time: " + e.getMessage(), dateTimeStr, 0);
        }
    }
    
    @Override
    public boolean canHandle(String messageType) {
        // Este parser genérico puede manejar cualquier tipo de mensaje XML
        return messageType != null && (
            messageType.startsWith("PAIN") ||
            messageType.startsWith("PACS") ||
            messageType.startsWith("CAMT") ||
            messageType.equals("UNKNOWN")
        );
    }
    
    @Override
    public String getMessageType() {
        return "GENERIC_XML";
    }
}
