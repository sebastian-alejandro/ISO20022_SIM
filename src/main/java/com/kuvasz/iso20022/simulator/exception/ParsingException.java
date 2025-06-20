package com.kuvasz.iso20022.simulator.exception;

import com.kuvasz.iso20022.simulator.model.ValidationError;
import java.util.List;

/**
 * Excepción específica para errores de parsing XML
 */
public class ParsingException extends ISO20022Exception {
    
    private final List<ValidationError> validationErrors;
    private final String xpath;
    
    public ParsingException(String message) {
        super("PARSING_ERROR", message);
        this.validationErrors = null;
        this.xpath = null;
    }
    
    public ParsingException(String message, Throwable cause) {
        super("PARSING_ERROR", message, cause);
        this.validationErrors = null;
        this.xpath = null;
    }
    
    public ParsingException(String message, String messageId) {
        super("PARSING_ERROR", message, messageId);
        this.validationErrors = null;
        this.xpath = null;
    }
    
    public ParsingException(String message, String xpath, String messageId) {
        super("PARSING_ERROR", message, messageId);
        this.xpath = xpath;
        this.validationErrors = null;
    }
    
    public ParsingException(String message, List<ValidationError> validationErrors) {
        super("PARSING_ERROR", message);
        this.validationErrors = validationErrors;
        this.xpath = null;
    }
    
    public ParsingException(String message, List<ValidationError> validationErrors, String messageId) {
        super("PARSING_ERROR", message, messageId);
        this.validationErrors = validationErrors;
        this.xpath = null;
    }
    
    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
    
    public String getXpath() {
        return xpath;
    }
    
    public boolean hasValidationErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }
}
