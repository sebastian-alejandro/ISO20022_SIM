package com.kuvasz.iso20022.simulator.model;

/**
 * Representa un error de validación encontrado durante el procesamiento
 */
public class ValidationError {
    
    public enum ErrorType {
        STRUCTURAL,
        BUSINESS_RULE,
        FORMAT,
        MISSING_FIELD,
        INVALID_VALUE,
        SCHEMA_VIOLATION
    }
    
    private ErrorType type;
    private String code;
    private String message;
    private String field;
    private String xpath;
    private Object actualValue;
    private Object expectedValue;
    
    public ValidationError() {}
    
    public ValidationError(ErrorType type, String code, String message) {
        this.type = type;
        this.code = code;
        this.message = message;
    }
    
    public ValidationError(ErrorType type, String code, String message, String field) {
        this(type, code, message);
        this.field = field;
    }
    
    // Static factory methods for common error types
    public static ValidationError structuralError(String code, String message, String xpath) {
        ValidationError error = new ValidationError(ErrorType.STRUCTURAL, code, message);
        error.setXpath(xpath);
        return error;
    }
    
    public static ValidationError businessRuleError(String code, String message, String field) {
        return new ValidationError(ErrorType.BUSINESS_RULE, code, message, field);
    }
    
    public static ValidationError formatError(String code, String message, String field, Object actualValue) {
        ValidationError error = new ValidationError(ErrorType.FORMAT, code, message, field);
        error.setActualValue(actualValue);
        return error;
    }
    
    public static ValidationError missingFieldError(String field) {
        return new ValidationError(ErrorType.MISSING_FIELD, "MISSING_FIELD", 
            "Required field is missing: " + field, field);
    }
    
    public static ValidationError invalidValueError(String field, Object actualValue, Object expectedValue) {
        ValidationError error = new ValidationError(ErrorType.INVALID_VALUE, "INVALID_VALUE", 
            String.format("Invalid value for field %s. Expected: %s, Actual: %s", field, expectedValue, actualValue), field);
        error.setActualValue(actualValue);
        error.setExpectedValue(expectedValue);
        return error;
    }
    
    // Getters and Setters
    public ErrorType getType() {
        return type;
    }
    
    public void setType(ErrorType type) {
        this.type = type;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public String getXpath() {
        return xpath;
    }
    
    public void setXpath(String xpath) {
        this.xpath = xpath;
    }
    
    public Object getActualValue() {
        return actualValue;
    }
    
    public void setActualValue(Object actualValue) {
        this.actualValue = actualValue;
    }
    
    public Object getExpectedValue() {
        return expectedValue;
    }
    
    public void setExpectedValue(Object expectedValue) {
        this.expectedValue = expectedValue;
    }
    
    @Override
    public String toString() {
        return String.format("ValidationError{type=%s, code='%s', message='%s', field='%s'}", 
            type, code, message, field);
    }
}
