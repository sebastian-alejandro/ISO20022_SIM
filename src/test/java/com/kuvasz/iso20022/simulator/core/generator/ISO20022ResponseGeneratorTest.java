package com.kuvasz.iso20022.simulator.core.generator;

import com.kuvasz.iso20022.simulator.model.MessageContext;
import com.kuvasz.iso20022.simulator.model.ProcessingResult;
import com.kuvasz.iso20022.simulator.model.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para ISO20022ResponseGenerator
 */
@ExtendWith(MockitoExtension.class)
class ISO20022ResponseGeneratorTest {

    private ISO20022ResponseGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ISO20022ResponseGenerator();
    }

    @Test
    void testCanHandle() {
        assertTrue(generator.canHandle("pacs.008.001.08"));
        assertTrue(generator.canHandle("pain.001.001.11"));
        assertTrue(generator.canHandle("pacs.004.001.09"));
        assertTrue(generator.canHandle("camt.053.001.08"));
        assertFalse(generator.canHandle("unsupported.message.type"));
        assertFalse(generator.canHandle(null));
    }

    @Test
    void testGetResponseType() {
        assertEquals("ISO20022_XML", generator.getResponseType());
    }

    @Test
    void testGenerateSuccessResponseForPacs008() {
        MessageContext originalContext = createMessageContext("pacs.008.001.08", "MSG123456789");
        ProcessingResult result = createSuccessResult();
        
        String response = generator.generateResponse(originalContext, result);
        
        assertNotNull(response);
        assertTrue(response.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(response.contains("pacs.002.001.10"));
        assertTrue(response.contains("FIToFIPmtStsRpt"));
        assertTrue(response.contains("MSG123456789"));
        assertTrue(response.contains("ACCP")); // Accepted status
        assertTrue(response.contains("pacs.008.001.08"));
    }

    @Test
    void testGenerateErrorResponseForPacs008() {
        MessageContext originalContext = createMessageContext("pacs.008.001.08", "MSG123456789");
        ProcessingResult result = createErrorResult();
        
        String response = generator.generateResponse(originalContext, result);
        
        assertNotNull(response);
        assertTrue(response.contains("RJCT")); // Rejected status
        assertTrue(response.contains("Validation errors found"));
        assertTrue(response.contains("TEST_ERROR"));
    }

    @Test
    void testGenerateSuccessResponseForPain001() {
        MessageContext originalContext = createMessageContext("pain.001.001.11", "PAIN123456789");
        ProcessingResult result = createSuccessResult();
        
        String response = generator.generateResponse(originalContext, result);
        
        assertNotNull(response);
        assertTrue(response.contains("pain.002.001.10"));
        assertTrue(response.contains("CstmrPmtStsRpt"));
        assertTrue(response.contains("PAIN123456789"));
        assertTrue(response.contains("ACCP")); // Accepted status
        assertTrue(response.contains("pain.001.001.11"));
    }

    @Test
    void testGenerateErrorResponseForPain001() {
        MessageContext originalContext = createMessageContext("pain.001.001.11", "PAIN123456789");
        ProcessingResult result = createErrorResult();
        
        String response = generator.generateResponse(originalContext, result);
        
        assertNotNull(response);
        assertTrue(response.contains("RJCT")); // Rejected status
        assertTrue(response.contains("Validation errors found"));
    }

    @Test
    void testGenerateResponseForPacs004() {
        MessageContext originalContext = createMessageContext("pacs.004.001.09", "RTR123456789");
        ProcessingResult result = createSuccessResult();
        
        String response = generator.generateResponse(originalContext, result);
        
        assertNotNull(response);
        assertTrue(response.contains("pacs.004.001.09"));
        assertTrue(response.contains("PmtRtr"));
        assertTrue(response.contains("RTR123456789"));
        assertTrue(response.contains("DUPL")); // Default return reason for success
    }

    @Test
    void testGenerateGenericResponse() {
        MessageContext originalContext = createMessageContext("unknown.message.type", "UNK123456789");
        ProcessingResult result = createErrorResult();
        
        String response = generator.generateResponse(originalContext, result);
        
        assertNotNull(response);
        assertTrue(response.contains("admi.002.001.01"));
        assertTrue(response.contains("MsgRjct"));
        assertTrue(response.contains("UNK123456789"));
        assertTrue(response.contains("RJCT"));
    }

    @Test
    void testGenerateResponseWithMultipleErrors() {
        MessageContext originalContext = createMessageContext("pacs.008.001.08", "MSG123456789");
        ProcessingResult result = createMultipleErrorsResult();
        
        String response = generator.generateResponse(originalContext, result);
        
        assertNotNull(response);
        assertTrue(response.contains("RJCT"));
        assertTrue(response.contains("3 errors"));
        assertTrue(response.contains("ERROR_1"));
        assertTrue(response.contains("ERROR_2"));
        assertTrue(response.contains("ERROR_3"));
    }

    @Test
    void testGenerateResponseWithWarnings() {
        MessageContext originalContext = createMessageContext("pacs.008.001.08", "MSG123456789");
        ProcessingResult result = createWarningResult();
        
        String response = generator.generateResponse(originalContext, result);
        
        assertNotNull(response);
        assertTrue(response.contains("ACSP")); // Accepted Settlement In Process
    }

    @Test
    void testResponseContainsValidXML() {
        MessageContext originalContext = createMessageContext("pacs.008.001.08", "MSG123456789");
        ProcessingResult result = createSuccessResult();
        
        String response = generator.generateResponse(originalContext, result);
        
        // Verificar que la respuesta sea XML válido básico
        assertTrue(response.startsWith("<?xml"));
        assertTrue(response.contains("<Document"));
        assertTrue(response.endsWith("</Document>"));
        
        // Contar etiquetas de apertura y cierre
        long openTags = response.chars().filter(ch -> ch == '<').count();
        long closeTags = response.chars().filter(ch -> ch == '>').count();
        assertEquals(openTags, closeTags);
    }

    @Test
    void testResponseIncludesSenderAndReceiverInfo() {
        MessageContext originalContext = createMessageContext("pacs.008.001.08", "MSG123456789");
        originalContext.setSenderId("SENDERXXXX");
        originalContext.setReceiverId("RECEIVRXXX");
        
        ProcessingResult result = createSuccessResult();
        
        String response = generator.generateResponse(originalContext, result);
        
        assertTrue(response.contains("SENDERXXXX"));
        assertTrue(response.contains("RECEIVRXXX"));
    }

    private MessageContext createMessageContext(String messageType, String messageId) {
        MessageContext context = new MessageContext();
        context.setMessageType(messageType);
        context.setMessageId(messageId);
        context.setSenderId("TESTBIC1XXX");
        context.setReceiverId("TESTBIC2XXX");
        return context;
    }

    private ProcessingResult createSuccessResult() {
        ProcessingResult result = new ProcessingResult();
        result.setStatus(ProcessingResult.Status.SUCCESS);
        result.setMessageId("MSG123456789");
        result.setMessageType("pacs.008.001.08");
        return result;
    }

    private ProcessingResult createErrorResult() {
        ProcessingResult result = new ProcessingResult();
        result.setStatus(ProcessingResult.Status.ERROR);
        result.setMessageId("MSG123456789");
        result.setMessageType("pacs.008.001.08");
        
        ValidationError error = ValidationError.businessRuleError("TEST_ERROR", "Test error message", "TestField");
        result.setErrors(List.of(error));
        
        return result;
    }

    private ProcessingResult createMultipleErrorsResult() {
        ProcessingResult result = new ProcessingResult();
        result.setStatus(ProcessingResult.Status.ERROR);
        result.setMessageId("MSG123456789");
        result.setMessageType("pacs.008.001.08");
        
        List<ValidationError> errors = List.of(
            ValidationError.businessRuleError("ERROR_1", "First error", "Field1"),
            ValidationError.businessRuleError("ERROR_2", "Second error", "Field2"),
            ValidationError.businessRuleError("ERROR_3", "Third error", "Field3")
        );
        result.setErrors(errors);
        
        return result;
    }

    private ProcessingResult createWarningResult() {
        ProcessingResult result = new ProcessingResult();
        result.setStatus(ProcessingResult.Status.WARNING);
        result.setMessageId("MSG123456789");
        result.setMessageType("pacs.008.001.08");
        return result;
    }
}
