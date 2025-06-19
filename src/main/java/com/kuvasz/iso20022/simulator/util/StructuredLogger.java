package com.kuvasz.iso20022.simulator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Utilidad para logging estructurado con MDC (Mapped Diagnostic Context)
 */
@Component
public class StructuredLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(StructuredLogger.class);
    
    // Constantes para keys de MDC
    public static final String MESSAGE_ID = "messageId";
    public static final String MESSAGE_TYPE = "messageType";
    public static final String PROCESSING_TIME = "processingTimeMs";
    public static final String COMPONENT = "component";
    public static final String OPERATION = "operation";
    public static final String STATUS = "status";
    public static final String CLIENT_IP = "clientIp";
    public static final String USER_AGENT = "userAgent";
    public static final String CORRELATION_ID = "correlationId";
    
    /**
     * Registra el inicio del procesamiento de un mensaje
     */
    public void logMessageStart(String messageId, String messageType, String clientIp) {
        MDC.put(MESSAGE_ID, messageId);
        MDC.put(MESSAGE_TYPE, messageType);
        MDC.put(COMPONENT, "message-processor");
        MDC.put(OPERATION, "process-start");
        MDC.put(STATUS, "STARTED");
        MDC.put(CLIENT_IP, clientIp);
        MDC.put(CORRELATION_ID, generateCorrelationId());
        
        logger.info("Starting message processing");
    }
    
    /**
     * Registra la finalización exitosa del procesamiento
     */
    public void logMessageSuccess(String messageId, String messageType, long processingTimeMs) {
        MDC.put(MESSAGE_ID, messageId);
        MDC.put(MESSAGE_TYPE, messageType);
        MDC.put(PROCESSING_TIME, String.valueOf(processingTimeMs));
        MDC.put(COMPONENT, "message-processor");
        MDC.put(OPERATION, "process-complete");
        MDC.put(STATUS, "SUCCESS");
        
        logger.info("Message processed successfully");
        
        MDC.clear();
    }
    
    /**
     * Registra un error en el procesamiento
     */
    public void logMessageError(String messageId, String messageType, String errorCode, 
                               String errorMessage, long processingTimeMs, Throwable throwable) {
        MDC.put(MESSAGE_ID, messageId);
        MDC.put(MESSAGE_TYPE, messageType);
        MDC.put(PROCESSING_TIME, String.valueOf(processingTimeMs));
        MDC.put(COMPONENT, "message-processor");
        MDC.put(OPERATION, "process-error");
        MDC.put(STATUS, "ERROR");
        MDC.put("errorCode", errorCode);
        MDC.put("errorMessage", errorMessage);
        
        if (throwable != null) {
            logger.error("Error processing message", throwable);
        } else {
            logger.error("Error processing message: {}", errorMessage);
        }
        
        MDC.clear();
    }
    
    /**
     * Registra métricas de performance
     */
    public void logPerformanceMetrics(int currentTps, int avgResponseTime, int activeConnections) {
        MDC.put(COMPONENT, "performance-monitor");
        MDC.put(OPERATION, "metrics-snapshot");
        MDC.put("currentTps", String.valueOf(currentTps));
        MDC.put("avgResponseTimeMs", String.valueOf(avgResponseTime));
        MDC.put("activeConnections", String.valueOf(activeConnections));
        
        logger.info("Performance metrics snapshot");
        
        MDC.clear();
    }
    
    /**
     * Registra eventos de base de datos
     */
    public void logDatabaseOperation(String operation, String table, long executionTimeMs, boolean success) {
        MDC.put(COMPONENT, "database");
        MDC.put(OPERATION, operation);
        MDC.put("table", table);
        MDC.put("executionTimeMs", String.valueOf(executionTimeMs));
        MDC.put(STATUS, success ? "SUCCESS" : "ERROR");
        
        logger.info("Database operation completed");
        
        MDC.clear();
    }
    
    /**
     * Registra eventos de validación
     */
    public void logValidationEvent(String messageId, String validationType, boolean isValid, String validationErrors) {
        MDC.put(MESSAGE_ID, messageId);
        MDC.put(COMPONENT, "validator");
        MDC.put(OPERATION, validationType);
        MDC.put(STATUS, isValid ? "VALID" : "INVALID");
        
        if (!isValid && validationErrors != null) {
            MDC.put("validationErrors", validationErrors);
        }
        
        logger.info("Message validation completed");
        
        MDC.clear();
    }
    
    /**
     * Genera un ID de correlación único
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Utility para medir tiempo de ejecución
     */
    public static class ExecutionTimer {
        private final Instant start;
        
        public ExecutionTimer() {
            this.start = Instant.now();
        }
        
        public long getElapsedMillis() {
            return Duration.between(start, Instant.now()).toMillis();
        }
    }
}
