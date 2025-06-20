package com.kuvasz.iso20022.simulator.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Representa el resultado del procesamiento de un mensaje ISO 20022
 */
public class ProcessingResult {
    
    public enum Status {
        SUCCESS,
        WARNING,
        ERROR,
        VALIDATION_FAILED
    }
    
    private Status status;
    private String messageId;
    private String messageType;
    private LocalDateTime processedAt;
    private String originalMessage;
    private String processedMessage;
    private List<ValidationError> errors;
    private List<String> warnings;
    private long processingTimeMs;
    
    public ProcessingResult() {
        this.processedAt = LocalDateTime.now();
    }
    
    public ProcessingResult(Status status, String messageId, String messageType) {
        this();
        this.status = status;
        this.messageId = messageId;
        this.messageType = messageType;
    }
    
    // Getters and Setters
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public String getOriginalMessage() {
        return originalMessage;
    }
    
    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }
    
    public String getProcessedMessage() {
        return processedMessage;
    }
    
    public void setProcessedMessage(String processedMessage) {
        this.processedMessage = processedMessage;
    }
    
    public List<ValidationError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
    
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
}
