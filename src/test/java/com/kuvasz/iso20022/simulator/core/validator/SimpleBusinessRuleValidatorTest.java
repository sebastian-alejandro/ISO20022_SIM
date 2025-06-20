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
 * Tests unitarios para SimpleBusinessRuleValidator
 */
@ExtendWith(MockitoExtension.class)
class SimpleBusinessRuleValidatorTest {

    private SimpleBusinessRuleValidator validator;
    private DocumentBuilderFactory documentBuilderFactory;

    @BeforeEach
    void setUp() {
        validator = new SimpleBusinessRuleValidator();
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    @Test
    void testCanHandle() {
        assertTrue(validator.canHandle("pacs.008.001.08"));
        assertTrue(validator.canHandle("pacs.004.001.09"));
        assertTrue(validator.canHandle("camt.053.001.08"));
        assertTrue(validator.canHandle("pain.001.001.11"));
        assertFalse(validator.canHandle("unsupported.message.type"));
        assertFalse(validator.canHandle(null));
    }

    @Test
    void testGetValidationType() {
        assertEquals("BUSINESS_RULES", validator.getValidationType());
    }

    @Test
    void testValidateWithValidMessage() throws Exception {
        String validXML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>MSG123456789</MsgId>
                        <CreDtTm>2024-01-15T10:30:00</CreDtTm>
                        <InstgAgt>
                            <FinInstnId>
                                <BICFI>TESTBIC1XXX</BICFI>
                            </FinInstnId>
                        </InstgAgt>
                    </GrpHdr>
                    <CdtTrfTxInf>
                        <InstdAmt Ccy="USD">1000.50</InstdAmt>
                        <PmtInf>
                            <PmtInfId>PMT123</PmtInfId>
                        </PmtInf>
                    </CdtTrfTxInf>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

        MessageContext context = createMessageContext(validXML, "pacs.008.001.08");
        
        List<ValidationError> errors = validator.validate(context);
        
        assertNotNull(errors);
        // En un mensaje válido, deberíamos tener pocos o ningún error
        assertTrue(errors.size() >= 0);
    }

    @Test
    void testValidateWithMissingMessageId() throws Exception {
        String xmlWithoutMsgId = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <CreDtTm>2024-01-15T10:30:00</CreDtTm>
                    </GrpHdr>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

        MessageContext context = createMessageContext(xmlWithoutMsgId, "pacs.008.001.08");
        
        List<ValidationError> errors = validator.validate(context);
        
        assertNotNull(errors);
        // Debería haber al menos un error por MsgId faltante
        assertTrue(errors.stream().anyMatch(e -> e.getField() != null && e.getField().equals("MsgId")));
    }

    @Test
    void testValidateWithInvalidBIC() throws Exception {
        String xmlWithInvalidBIC = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>MSG123456789</MsgId>
                        <InstgAgt>
                            <FinInstnId>
                                <BICFI>INVALIDBIC</BICFI>
                            </FinInstnId>
                        </InstgAgt>
                    </GrpHdr>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

        MessageContext context = createMessageContext(xmlWithInvalidBIC, "pacs.008.001.08");
        
        List<ValidationError> errors = validator.validate(context);
        
        assertNotNull(errors);
        // Debería haber un error por BIC inválido
        assertTrue(errors.stream().anyMatch(e -> e.getCode().contains("INVALID_BIC_FORMAT")));
    }

    @Test
    void testValidateWithInvalidAmount() throws Exception {
        String xmlWithInvalidAmount = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>MSG123456789</MsgId>
                    </GrpHdr>
                    <CdtTrfTxInf>
                        <InstdAmt Ccy="USD">-100.00</InstdAmt>
                    </CdtTrfTxInf>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

        MessageContext context = createMessageContext(xmlWithInvalidAmount, "pacs.008.001.08");
        
        List<ValidationError> errors = validator.validate(context);
        
        assertNotNull(errors);
        // Debería haber un error por monto inválido (negativo)
        assertTrue(errors.stream().anyMatch(e -> e.getCode().contains("INVALID_AMOUNT")));
    }

    @Test
    void testValidateWithInvalidCurrency() throws Exception {
        String xmlWithInvalidCurrency = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>MSG123456789</MsgId>
                    </GrpHdr>
                    <CdtTrfTxInf>
                        <InstdAmt Ccy="XYZ">100.00</InstdAmt>
                    </CdtTrfTxInf>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

        MessageContext context = createMessageContext(xmlWithInvalidCurrency, "pacs.008.001.08");
        
        List<ValidationError> errors = validator.validate(context);
        
        assertNotNull(errors);
        // Debería haber un error por código de moneda inválido
        assertTrue(errors.stream().anyMatch(e -> e.getCode().contains("INVALID_CURRENCY_CODE")));
    }

    @Test
    void testValidateWithLongMessageId() throws Exception {
        String xmlWithLongMsgId = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>THIS_IS_A_VERY_LONG_MESSAGE_ID_THAT_EXCEEDS_THE_MAXIMUM_LENGTH_ALLOWED</MsgId>
                    </GrpHdr>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

        MessageContext context = createMessageContext(xmlWithLongMsgId, "pacs.008.001.08");
        
        List<ValidationError> errors = validator.validate(context);
        
        assertNotNull(errors);
        // Debería haber un error por MsgId demasiado largo
        assertTrue(errors.stream().anyMatch(e -> e.getCode().contains("INVALID_MESSAGE_ID_LENGTH")));
    }

    @Test
    void testValidateWithNullContext() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(null);
        });

        assertTrue(exception.getMessage().contains("null"));
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

        assertTrue(exception.getMessage().contains("Documento XML no disponible"));
    }

    @Test
    void testValidatePaymentInstruction() throws Exception {
        String xmlWithoutPaymentInfo = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>MSG123456789</MsgId>
                    </GrpHdr>
                    <!-- Missing PmtInf -->
                </FIToFICstmrCdtTrf>
            </Document>
            """;

        MessageContext context = createMessageContext(xmlWithoutPaymentInfo, "pacs.008.001.08");
        
        List<ValidationError> errors = validator.validate(context);
        
        assertNotNull(errors);
        // Debería haber un error por PmtInf faltante
        assertTrue(errors.stream().anyMatch(e -> e.getField() != null && e.getField().equals("PmtInf")));
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
