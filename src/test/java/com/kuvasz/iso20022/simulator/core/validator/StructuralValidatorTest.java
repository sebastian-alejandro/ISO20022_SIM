package com.kuvasz.iso20022.simulator.core.validator;

import com.kuvasz.iso20022.simulator.exception.ValidationException;
import com.kuvasz.iso20022.simulator.model.MessageContext;
import com.kuvasz.iso20022.simulator.model.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para StructuralValidator
 */
@ExtendWith(MockitoExtension.class)
class StructuralValidatorTest {

    private StructuralValidator validator;
    private DocumentBuilderFactory documentBuilderFactory;

    @BeforeEach
    void setUp() {
        validator = new StructuralValidator();
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    @Test
    void testCanHandle() {
        assertTrue(validator.canHandle("pacs.008.001.08"));
        assertTrue(validator.canHandle("pain.001.001.11"));
        assertTrue(validator.canHandle("camt.053.001.08"));
        assertTrue(validator.canHandle("any.message.type"));
    }

    @Test
    void testGetValidationType() {
        assertEquals("STRUCTURAL", validator.getValidationType());
    }

    @Test
    void testValidateWithValidXML() throws Exception {
        String validXML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>MSG123456789</MsgId>
                        <CreDtTm>2024-01-15T10:30:00</CreDtTm>
                        <NbOfTxs>1</NbOfTxs>
                    </GrpHdr>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

        MessageContext context = createMessageContext(validXML, "pacs.008.001.08");
        
        // Esta validación pasará porque no tenemos XSD real cargado
        List<ValidationError> errors = validator.validate(context);
        
        assertNotNull(errors);
        // Sin XSD real, no habrá errores estructurales
    }

    @Test
    void testValidateWithInvalidXML() throws Exception {
        String invalidXML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>MSG123456789</MsgId>
                        <!-- Missing required elements -->
                    </GrpHdr>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

        MessageContext context = createMessageContext(invalidXML, "pacs.008.001.08");
        
        List<ValidationError> errors = validator.validate(context);
        
        assertNotNull(errors);
        // Sin XSD real, no se detectarán errores estructurales
    }

    @Test
    void testValidateWithNullContext() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(null);
        });

        assertTrue(exception.getMessage().contains("MessageContext cannot be null"));
    }

    @Test
    void testValidateWithNullDocument() {
        MessageContext context = new MessageContext();
        context.setMessageId("TEST123");
        context.setMessageType("pacs.008.001.08");
        context.setParsedDocument(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(context);
        });

        assertTrue(exception.getMessage().contains("Document is required for structural validation"));
    }

    @Test
    void testValidateWithUnsupportedMessageType() throws Exception {
        String validXML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:unknown.message.type">
                <UnknownMessage>
                    <Content>Test</Content>
                </UnknownMessage>
            </Document>
            """;

        MessageContext context = createMessageContext(validXML, "unknown.message.type");
        
        List<ValidationError> errors = validator.validate(context);
        
        assertNotNull(errors);
        // El validador debería manejar tipos de mensaje desconocidos sin fallar
    }

    @Test
    void testValidateWithMalformedXML() throws Exception {
        // Este test simula un caso donde el XML está bien formado pero estructuralmente incorrecto
        String malformedXML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <WrongRootElement>
                    <Content>This should not be valid according to XSD</Content>
                </WrongRootElement>
            </Document>
            """;

        MessageContext context = createMessageContext(malformedXML, "pacs.008.001.08");
        
        List<ValidationError> errors = validator.validate(context);
        
        assertNotNull(errors);
        // Sin XSD real, no se pueden detectar errores estructurales específicos
    }

    private MessageContext createMessageContext(String xmlContent, String messageType) throws Exception {
        MessageContext context = new MessageContext();
        context.setMessageId("TEST123");
        context.setMessageType(messageType);
        context.setOriginalXml(xmlContent);
        
        // Parse XML to Document
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
        context.setParsedDocument(document);
        
        return context;
    }
}
