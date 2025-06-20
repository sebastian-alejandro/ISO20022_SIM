package com.kuvasz.iso20022.simulator.exception;

/**
 * Excepción base para errores relacionados con ISO 20022
 */
public class ISO20022Exception extends Exception {
    
    private final String errorCode;
    private final String messageId;
    
    public ISO20022Exception(String message) {
        super(message);
        this.errorCode = "ISO20022_ERROR";
        this.messageId = null;
    }
    
    public ISO20022Exception(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ISO20022_ERROR";
        this.messageId = null;
    }
    
    public ISO20022Exception(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.messageId = null;
    }
    
    public ISO20022Exception(String errorCode, String message, String messageId) {
        super(message);
        this.errorCode = errorCode;
        this.messageId = messageId;
    }
    
    public ISO20022Exception(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.messageId = null;
    }
    
    public ISO20022Exception(String errorCode, String message, String messageId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.messageId = messageId;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    @Override
    public String toString() {
        return String.format("ISO20022Exception{errorCode='%s', messageId='%s', message='%s'}", 
            errorCode, messageId, getMessage());
    }
}
