package com.kuvasz.iso20022.simulator.integration;

import com.kuvasz.iso20022.simulator.core.generator.ISO20022ResponseGenerator;
import com.kuvasz.iso20022.simulator.core.parser.GenericXMLParser;
import com.kuvasz.iso20022.simulator.core.validator.SimpleBusinessRuleValidator;
import com.kuvasz.iso20022.simulator.core.validator.StructuralValidator;
import com.kuvasz.iso20022.simulator.model.MessageContext;
import com.kuvasz.iso20022.simulator.model.ProcessingResult;
import com.kuvasz.iso20022.simulator.model.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para el procesamiento completo de mensajes ISO 20022
 */
@SpringBootTest
class ISO20022ProcessingIntegrationTest {

    private GenericXMLParser parser;
    private StructuralValidator structuralValidator;
    private SimpleBusinessRuleValidator businessRuleValidator;
    private ISO20022ResponseGenerator responseGenerator;

    @BeforeEach
    void setUp() {
        parser = new GenericXMLParser();
        structuralValidator = new StructuralValidator();
        businessRuleValidator = new SimpleBusinessRuleValidator();
        responseGenerator = new ISO20022ResponseGenerator();
    }

    @Test
    void testCompleteProcessingFlowWithValidMessage() throws Exception {
        String validPacs008XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>VALID123456789</MsgId>
                        <CreDtTm>2024-01-15T10:30:00</CreDtTm>
                        <NbOfTxs>1</NbOfTxs>
                        <CtrlSum>1000.00</CtrlSum>
                        <InstgAgt>
                            <FinInstnId>
                                <BICFI>TESTBIC1XXX</BICFI>
                            </FinInstnId>
                        </InstgAgt>
                        <InstdAgt>
                            <FinInstnId>
                                <BICFI>TESTBIC2XXX</BICFI>
                            </FinInstnId>
                        </InstdAgt>
                    </GrpHdr>
                    <CdtTrfTxInf>
                        <PmtId>
                            <EndToEndId>E2E123</EndToEndId>
                            <TxId>TX123</TxId>
                        </PmtId>
                        <InstdAmt Ccy="USD">1000.00</InstdAmt>
                        <PmtInf>
                            <PmtInfId>PMT123</PmtInfId>
                        </PmtInf>
                        <Dbtr>
                            <Nm>John Doe</Nm>
                        </Dbtr>
                        <Cdtr>
                            <Nm>Jane Smith</Nm>
                        </Cdtr>
                    </CdtTrfTxInf>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

        // Paso 1: Parsing
        MessageContext context = parser.parse(validPacs008XML);
        assertNotNull(context);
        assertEquals("VALID123456789", context.getMessageId());
        assertEquals("pacs.008.001.08", context.getMessageType());

        // Paso 2: Validación estructural
        List<ValidationError> structuralErrors = structuralValidator.validate(context);
        assertNotNull(structuralErrors);

        // Paso 3: Validación de reglas de negocio
        List<ValidationError> businessErrors = businessRuleValidator.validate(context);
        assertNotNull(businessErrors);

        // Paso 4: Crear resultado del procesamiento
        ProcessingResult result = createProcessingResult(context, structuralErrors, businessErrors);
        
        // Paso 5: Generar respuesta
        String response = responseGenerator.generateResponse(context, result);
        assertNotNull(response);
        assertTrue(response.contains("pacs.002.001.10"));
        assertTrue(response.contains("VALID123456789"));
        
        if (result.getStatus() == ProcessingResult.Status.SUCCESS) {
            assertTrue(response.contains("ACCP"));
        }
    }

    @Test
    void testCompleteProcessingFlowWithInvalidMessage() throws Exception {
        String invalidPacs008XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>THIS_IS_A_VERY_LONG_MESSAGE_ID_THAT_EXCEEDS_THE_MAXIMUM_LENGTH_ALLOWED_BY_ISO20022</MsgId>
                        <CreDtTm>invalid-date-format</CreDtTm>
                        <InstgAgt>
                            <FinInstnId>
                                <BICFI>INVALIDBIC</BICFI>
                            </FinInstnId>
                        </InstgAgt>
                    </GrpHdr>
                    <CdtTrfTxInf>
                        <InstdAmt Ccy="XYZ">-100.00</InstdAmt>
                        <!-- Missing PmtInf -->
                    </CdtTrfTxInf>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

        // Paso 1: Parsing (debe ser exitoso aunque el contenido sea inválido)
        MessageContext context = parser.parse(invalidPacs008XML);
        assertNotNull(context);

        // Paso 2: Validación estructural
        List<ValidationError> structuralErrors = structuralValidator.validate(context);
        assertNotNull(structuralErrors);

        // Paso 3: Validación de reglas de negocio (debe encontrar errores)
        List<ValidationError> businessErrors = businessRuleValidator.validate(context);
        assertNotNull(businessErrors);
        assertTrue(businessErrors.size() > 0); // Debería haber errores

        // Verificar que se encontraron errores específicos
        assertTrue(businessErrors.stream().anyMatch(e -> e.getCode().contains("INVALID_MESSAGE_ID_LENGTH")));
        assertTrue(businessErrors.stream().anyMatch(e -> e.getCode().contains("INVALID_BIC_FORMAT")));
        assertTrue(businessErrors.stream().anyMatch(e -> e.getCode().contains("INVALID_CURRENCY_CODE")));

        // Paso 4: Crear resultado del procesamiento (con errores)
        ProcessingResult result = createProcessingResult(context, structuralErrors, businessErrors);
        assertEquals(ProcessingResult.Status.ERROR, result.getStatus());

        // Paso 5: Generar respuesta de error
        String response = responseGenerator.generateResponse(context, result);
        assertNotNull(response);
        assertTrue(response.contains("RJCT")); // Status de rechazo
        assertTrue(response.contains("Validation errors found"));
    }

    @Test
    void testCompleteProcessingFlowWithPain001Message() throws Exception {
        String validPain001XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.11">
                <CstmrCdtTrfInitn>
                    <GrpHdr>
                        <MsgId>PAIN123456789</MsgId>
                        <CreDtTm>2024-01-15T10:30:00</CreDtTm>
                        <NbOfTxs>1</NbOfTxs>
                        <InitgPty>
                            <Nm>Initiating Party</Nm>
                        </InitgPty>
                    </GrpHdr>
                    <PmtInf>
                        <PmtInfId>PMT789</PmtInfId>
                        <PmtMtd>TRF</PmtMtd>
                        <ReqdExctnDt>
                            <Dt>2024-01-16</Dt>
                        </ReqdExctnDt>
                        <Dbtr>
                            <Nm>Debtor Name</Nm>
                        </Dbtr>
                        <CdtTrfTxInf>
                            <PmtId>
                                <EndToEndId>E2E789</EndToEndId>
                            </PmtId>
                            <Amt>
                                <InstdAmt Ccy="EUR">500.00</InstdAmt>
                            </Amt>
                            <Cdtr>
                                <Nm>Creditor Name</Nm>
                            </Cdtr>
                        </CdtTrfTxInf>
                    </PmtInf>
                </CstmrCdtTrfInitn>
            </Document>
            """;

        // Procesamiento completo
        MessageContext context = parser.parse(validPain001XML);
        assertEquals("pain.001.001.11", context.getMessageType());
        assertEquals("PAIN123456789", context.getMessageId());

        List<ValidationError> structuralErrors = structuralValidator.validate(context);
        List<ValidationError> businessErrors = businessRuleValidator.validate(context);

        ProcessingResult result = createProcessingResult(context, structuralErrors, businessErrors);
        
        String response = responseGenerator.generateResponse(context, result);
        assertNotNull(response);
        assertTrue(response.contains("pain.002.001.10"));  // Customer Payment Status Report
        assertTrue(response.contains("CstmrPmtStsRpt"));
        assertTrue(response.contains("PAIN123456789"));
    }

    @Test
    void testParserAndValidatorCompatibility() throws Exception {
        String testXML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>COMPAT123</MsgId>
                        <CreDtTm>2024-01-15T10:30:00</CreDtTm>
                    </GrpHdr>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

        // El parser debe producir un contexto que los validadores puedan procesar
        MessageContext context = parser.parse(testXML);
        assertNotNull(context.getParsedDocument());

        // Los validadores deben poder procesar el contexto sin errores
        assertDoesNotThrow(() -> structuralValidator.validate(context));
        assertDoesNotThrow(() -> businessRuleValidator.validate(context));
    }

    @Test
    void testEndToEndProcessingWithDifferentMessageTypes() throws Exception {
        String[] messageTypes = {
            "pacs.008.001.08",
            "pain.001.001.11",
            "pacs.004.001.09"
        };

        for (String messageType : messageTypes) {
            String testXML = createTestXMLForMessageType(messageType);
            
            // Verificar que cada componente puede manejar el tipo de mensaje
            assertTrue(parser.canHandle(messageType));
            assertTrue(structuralValidator.canHandle(messageType));
            
            if (businessRuleValidator.canHandle(messageType)) {
                MessageContext context = parser.parse(testXML);
                assertNotNull(context);
                assertEquals(messageType, context.getMessageType());
                
                List<ValidationError> errors = businessRuleValidator.validate(context);
                assertNotNull(errors);
            }
            
            if (responseGenerator.canHandle(messageType)) {
                MessageContext context = parser.parse(testXML);
                ProcessingResult result = new ProcessingResult();
                result.setStatus(ProcessingResult.Status.SUCCESS);
                
                String response = responseGenerator.generateResponse(context, result);
                assertNotNull(response);
                assertTrue(response.contains("<?xml"));
            }
        }
    }

    private ProcessingResult createProcessingResult(MessageContext context, 
                                                   List<ValidationError> structuralErrors, 
                                                   List<ValidationError> businessErrors) {
        ProcessingResult result = new ProcessingResult();
        result.setMessageId(context.getMessageId());
        result.setMessageType(context.getMessageType());
        
        List<ValidationError> allErrors = new ArrayList<>();
        allErrors.addAll(structuralErrors);
        allErrors.addAll(businessErrors);
        
        result.setErrors(allErrors);
        
        if (allErrors.isEmpty()) {
            result.setStatus(ProcessingResult.Status.SUCCESS);
        } else {
            result.setStatus(ProcessingResult.Status.ERROR);
        }
        
        return result;
    }

    private String createTestXMLForMessageType(String messageType) {
        return switch (messageType) {
            case "pacs.008.001.08" -> """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                    <FIToFICstmrCdtTrf>
                        <GrpHdr>
                            <MsgId>TEST123</MsgId>
                            <CreDtTm>2024-01-15T10:30:00</CreDtTm>
                        </GrpHdr>
                    </FIToFICstmrCdtTrf>
                </Document>
                """;
            case "pain.001.001.11" -> """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.11">
                    <CstmrCdtTrfInitn>
                        <GrpHdr>
                            <MsgId>TEST123</MsgId>
                            <CreDtTm>2024-01-15T10:30:00</CreDtTm>
                        </GrpHdr>
                    </CstmrCdtTrfInitn>
                </Document>
                """;
            case "pacs.004.001.09" -> """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.004.001.09">
                    <PmtRtr>
                        <GrpHdr>
                            <MsgId>TEST123</MsgId>
                            <CreDtTm>2024-01-15T10:30:00</CreDtTm>
                        </GrpHdr>
                    </PmtRtr>
                </Document>
                """;
            default -> throw new IllegalArgumentException("Unsupported message type: " + messageType);
        };
    }
}
