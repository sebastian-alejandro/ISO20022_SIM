package com.kuvasz.iso20022.simulator.core.generator;

import com.kuvasz.iso20022.simulator.model.MessageContext;
import com.kuvasz.iso20022.simulator.model.ProcessingResult;
import com.kuvasz.iso20022.simulator.model.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Generador de respuestas XML para mensajes ISO 20022.
 * Genera respuestas de aceptación, rechazo y estado según el resultado del procesamiento.
 */
@Component
public class ISO20022ResponseGenerator implements ResponseGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ISO20022ResponseGenerator.class);
    
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public String generateResponse(MessageContext originalContext, ProcessingResult processingResult) {
        logger.debug("Generando respuesta para mensaje: {} con estado: {}", 
            originalContext.getMessageId(), processingResult.getStatus());
        
        String messageType = originalContext.getMessageType();
        
        // Determinar el tipo de respuesta según el mensaje original
        if (messageType.startsWith("pacs.008")) {
            return generatePaymentStatusResponse(originalContext, processingResult);
        } else if (messageType.startsWith("pain.001")) {
            return generateCustomerPaymentStatusResponse(originalContext, processingResult);
        } else if (messageType.startsWith("pacs.004")) {
            return generatePaymentReturnResponse(originalContext, processingResult);
        } else {
            return generateGenericStatusResponse(originalContext, processingResult);
        }
    }

    @Override
    public boolean canHandle(String messageType) {
        return messageType != null && (
            messageType.startsWith("pacs.008") ||
            messageType.startsWith("pain.001") ||
            messageType.startsWith("pacs.004") ||
            messageType.startsWith("camt.053")
        );
    }

    @Override
    public String getResponseType() {
        return "ISO20022_XML";
    }

    /**
     * Genera una respuesta de estado de pago (pacs.002) para mensajes pacs.008
     */
    private String generatePaymentStatusResponse(MessageContext originalContext, ProcessingResult processingResult) {
        String responseId = generateResponseId();
        String currentDateTime = LocalDateTime.now().format(ISO_DATETIME_FORMATTER);
        String status = mapProcessingStatusToISO(processingResult.getStatus());
        String statusReason = generateStatusReason(processingResult);
        
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10\">\n");
        xml.append("  <FIToFIPmtStsRpt>\n");
        xml.append("    <GrpHdr>\n");
        xml.append("      <MsgId>").append(responseId).append("</MsgId>\n");
        xml.append("      <CreDtTm>").append(currentDateTime).append("</CreDtTm>\n");
        xml.append("      <InstgAgt>\n");
        xml.append("        <FinInstnId>\n");
        xml.append("          <BICFI>").append(originalContext.getReceiverId() != null ? originalContext.getReceiverId() : "SIMULATRXXX").append("</BICFI>\n");
        xml.append("        </FinInstnId>\n");
        xml.append("      </InstgAgt>\n");
        xml.append("      <InstdAgt>\n");
        xml.append("        <FinInstnId>\n");
        xml.append("          <BICFI>").append(originalContext.getSenderId() != null ? originalContext.getSenderId() : "UNKNOWNXXXX").append("</BICFI>\n");
        xml.append("        </FinInstnId>\n");
        xml.append("      </InstdAgt>\n");
        xml.append("    </GrpHdr>\n");
        xml.append("    <OrgnlGrpInfAndSts>\n");
        xml.append("      <OrgnlMsgId>").append(originalContext.getMessageId()).append("</OrgnlMsgId>\n");
        xml.append("      <OrgnlMsgNmId>pacs.008.001.08</OrgnlMsgNmId>\n");
        xml.append("      <GrpSts>").append(status).append("</GrpSts>\n");
        
        if (!statusReason.isEmpty()) {
            xml.append("      <StsRsnInf>\n");
            xml.append("        <Rsn>\n");
            xml.append("          <Cd>").append(statusReason).append("</Cd>\n");
            xml.append("        </Rsn>\n");
            xml.append("      </StsRsnInf>\n");
        }
        
        // Agregar detalles de errores si existen
        if (processingResult.hasErrors()) {
            xml.append("      <StsRsnInf>\n");
            xml.append("        <AddtlInf>").append(generateErrorSummary(processingResult)).append("</AddtlInf>\n");
            xml.append("      </StsRsnInf>\n");
        }
        
        xml.append("    </OrgnlGrpInfAndSts>\n");
        xml.append("  </FIToFIPmtStsRpt>\n");
        xml.append("</Document>");
        
        return xml.toString();
    }

    /**
     * Genera una respuesta de estado de pago del cliente (pain.002) para mensajes pain.001
     */
    private String generateCustomerPaymentStatusResponse(MessageContext originalContext, ProcessingResult processingResult) {
        String responseId = generateResponseId();
        String currentDateTime = LocalDateTime.now().format(ISO_DATETIME_FORMATTER);
        String status = mapProcessingStatusToISO(processingResult.getStatus());
        
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.002.001.10\">\n");
        xml.append("  <CstmrPmtStsRpt>\n");
        xml.append("    <GrpHdr>\n");
        xml.append("      <MsgId>").append(responseId).append("</MsgId>\n");
        xml.append("      <CreDtTm>").append(currentDateTime).append("</CreDtTm>\n");
        xml.append("      <InitgPty>\n");
        xml.append("        <Nm>ISO20022 Simulator</Nm>\n");
        xml.append("      </InitgPty>\n");
        xml.append("    </GrpHdr>\n");
        xml.append("    <OrgnlGrpInfAndSts>\n");
        xml.append("      <OrgnlMsgId>").append(originalContext.getMessageId()).append("</OrgnlMsgId>\n");
        xml.append("      <OrgnlMsgNmId>pain.001.001.11</OrgnlMsgNmId>\n");
        xml.append("      <GrpSts>").append(status).append("</GrpSts>\n");
        
        // Agregar información de errores si existen
        if (processingResult.hasErrors()) {
            xml.append("      <StsRsnInf>\n");
            xml.append("        <AddtlInf>").append(generateErrorSummary(processingResult)).append("</AddtlInf>\n");
            xml.append("      </StsRsnInf>\n");
        }
        
        xml.append("    </OrgnlGrpInfAndSts>\n");
        xml.append("  </CstmrPmtStsRpt>\n");
        xml.append("</Document>");
        
        return xml.toString();
    }

    /**
     * Genera una respuesta de devolución de pago (pacs.004) 
     */
    private String generatePaymentReturnResponse(MessageContext originalContext, ProcessingResult processingResult) {
        String responseId = generateResponseId();
        String currentDateTime = LocalDateTime.now().format(ISO_DATETIME_FORMATTER);
        
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.004.001.09\">\n");
        xml.append("  <PmtRtr>\n");
        xml.append("    <GrpHdr>\n");
        xml.append("      <MsgId>").append(responseId).append("</MsgId>\n");
        xml.append("      <CreDtTm>").append(currentDateTime).append("</CreDtTm>\n");
        xml.append("      <NbOfTxs>1</NbOfTxs>\n");
        xml.append("      <InstgAgt>\n");
        xml.append("        <FinInstnId>\n");
        xml.append("          <BICFI>SIMULATRXXX</BICFI>\n");
        xml.append("        </FinInstnId>\n");
        xml.append("      </InstgAgt>\n");
        xml.append("    </GrpHdr>\n");
        xml.append("    <TxInf>\n");
        xml.append("      <RtrId>").append(responseId).append("</RtrId>\n");
        xml.append("      <OrgnlGrpInf>\n");
        xml.append("        <OrgnlMsgId>").append(originalContext.getMessageId()).append("</OrgnlMsgId>\n");
        xml.append("        <OrgnlMsgNmId>pacs.008.001.08</OrgnlMsgNmId>\n");
        xml.append("      </OrgnlGrpInf>\n");
        xml.append("      <RtrRsnInf>\n");
        xml.append("        <Rsn>\n");
        xml.append("          <Cd>").append(processingResult.hasErrors() ? "AC06" : "DUPL").append("</Cd>\n");
        xml.append("        </Rsn>\n");
        
        if (processingResult.hasErrors()) {
            xml.append("        <AddtlInf>").append(generateErrorSummary(processingResult)).append("</AddtlInf>\n");
        }
        
        xml.append("      </RtrRsnInf>\n");
        xml.append("    </TxInf>\n");
        xml.append("  </PmtRtr>\n");
        xml.append("</Document>");
        
        return xml.toString();
    }

    /**
     * Genera una respuesta genérica de estado
     */
    private String generateGenericStatusResponse(MessageContext originalContext, ProcessingResult processingResult) {
        String responseId = generateResponseId();
        String currentDateTime = LocalDateTime.now().format(ISO_DATETIME_FORMATTER);
        String status = mapProcessingStatusToISO(processingResult.getStatus());
        
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:admi.002.001.01\">\n");
        xml.append("  <MsgRjct>\n");
        xml.append("    <MsgHdr>\n");
        xml.append("      <MsgId>").append(responseId).append("</MsgId>\n");
        xml.append("      <CreDtTm>").append(currentDateTime).append("</CreDtTm>\n");
        xml.append("    </MsgHdr>\n");
        xml.append("    <RltdRef>\n");
        xml.append("      <Ref>").append(originalContext.getMessageId()).append("</Ref>\n");
        xml.append("    </RltdRef>\n");
        xml.append("    <Rsn>\n");
        xml.append("      <RsnCd>").append(status).append("</RsnCd>\n");
        
        if (processingResult.hasErrors()) {
            xml.append("      <AddtlRsnInf>").append(generateErrorSummary(processingResult)).append("</AddtlRsnInf>\n");
        }
        
        xml.append("    </Rsn>\n");
        xml.append("  </MsgRjct>\n");
        xml.append("</Document>");
        
        return xml.toString();
    }

    /**
     * Mapea el estado de procesamiento interno a códigos ISO 20022
     */
    private String mapProcessingStatusToISO(ProcessingResult.Status status) {
        return switch (status) {
            case SUCCESS -> "ACCP";  // Accepted
            case WARNING -> "ACSP";  // Accepted Settlement In Process
            case ERROR -> "RJCT";    // Rejected
            case VALIDATION_FAILED -> "RJCT"; // Rejected
            default -> "PDNG";       // Pending
        };
    }

    /**
     * Genera un código de razón de estado basado en el resultado del procesamiento
     */
    private String generateStatusReason(ProcessingResult processingResult) {
        if (processingResult.hasErrors()) {
            // Analizar el primer error para determinar el código de razón
            ValidationError firstError = processingResult.getErrors().get(0);
            return switch (firstError.getType()) {
                case STRUCTURAL -> "DS02"; // Format Error
                case BUSINESS_RULE -> "RR04"; // Regulatory Reason
                case FORMAT -> "FF01"; // Invalid File Format
                case MISSING_FIELD -> "AM05"; // Duplication
                case INVALID_VALUE -> "RF01"; // Not Unique Transaction Reference
                case SCHEMA_VIOLATION -> "DS02"; // Format Error
                default -> "MS03"; // Reason Not Specified
            };
        }
        return "";
    }

    /**
     * Genera un resumen de errores para incluir en la respuesta
     */
    private String generateErrorSummary(ProcessingResult processingResult) {
        if (!processingResult.hasErrors()) {
            return "No errors found";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Validation errors found: ");
        
        int errorCount = processingResult.getErrors().size();
        summary.append(errorCount).append(" error").append(errorCount > 1 ? "s" : "");
        
        // Agregar detalles de los primeros errores
        int maxErrors = Math.min(3, errorCount);
        for (int i = 0; i < maxErrors; i++) {
            ValidationError error = processingResult.getErrors().get(i);
            summary.append(". ").append(error.getCode()).append(": ").append(error.getMessage());
        }
        
        if (errorCount > maxErrors) {
            summary.append(" and ").append(errorCount - maxErrors).append(" more");
        }
        
        return summary.toString();
    }

    /**
     * Genera un ID único para la respuesta
     */
    private String generateResponseId() {
        return "SIM" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
