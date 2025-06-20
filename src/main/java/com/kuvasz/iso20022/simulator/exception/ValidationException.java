package com.kuvasz.iso20022.simulator.exception;

import com.kuvasz.iso20022.simulator.model.ValidationError;
import java.util.List;

/**
 * Excepción específica para errores de validación
 */
public class ValidationException extends ISO20022Exception {
    
    private final List<ValidationError> validationErrors;
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.validationErrors = null;
    }
    
    public ValidationException(String message, String messageId) {
        super("VALIDATION_ERROR", message, messageId);
        this.validationErrors = null;
    }
    
    public ValidationException(String message, List<ValidationError> validationErrors) {
        super("VALIDATION_ERROR", message);
        this.validationErrors = validationErrors;
    }
    
    public ValidationException(String message, List<ValidationError> validationErrors, String messageId) {
        super("VALIDATION_ERROR", message, messageId);
        this.validationErrors = validationErrors;
    }
    
    public ValidationException(ValidationError error) {
        super("VALIDATION_ERROR", error.getMessage());
        this.validationErrors = List.of(error);
    }
    
    public ValidationException(ValidationError error, String messageId) {
        super("VALIDATION_ERROR", error.getMessage(), messageId);
        this.validationErrors = List.of(error);
    }
    
    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
    
    public boolean hasValidationErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }
    
    public int getErrorCount() {
        return validationErrors != null ? validationErrors.size() : 0;
    }
}
