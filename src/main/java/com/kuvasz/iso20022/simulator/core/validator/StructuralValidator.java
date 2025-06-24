package com.kuvasz.iso20022.simulator.core.validator;

import com.kuvasz.iso20022.simulator.exception.ValidationException;
import com.kuvasz.iso20022.simulator.model.MessageContext;
import com.kuvasz.iso20022.simulator.model.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validador estructural XML usando esquemas XSD
 */
@Component
public class StructuralValidator implements MessageValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(StructuralValidator.class);
    
    private final SchemaFactory schemaFactory;
    private final Map<String, Schema> schemaCache;
    
    // Mapping de tipos de mensaje a archivos de esquema
    private static final Map<String, String> SCHEMA_MAPPINGS = new HashMap<>();
    static {
        SCHEMA_MAPPINGS.put("PAIN.001", "schemas/pain.001.001.03.xsd");
        SCHEMA_MAPPINGS.put("PACS.008", "schemas/pacs.008.001.02.xsd");
        SCHEMA_MAPPINGS.put("CAMT.053", "schemas/camt.053.001.02.xsd");
    }
    
    public StructuralValidator() {
        this.schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        this.schemaCache = new HashMap<>();
        
        // Configuración de seguridad
        try {
            schemaFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            schemaFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            schemaFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (SAXException e) {
            logger.warn("Could not configure schema factory security features: {}", e.getMessage());
        }
    }
      @Override
    public List<ValidationError> validate(MessageContext context) throws ValidationException {
        if (context == null) {
            throw new ValidationException("MessageContext cannot be null");
        }
        
        if (context.getOriginalXml() == null) {
            throw new ValidationException("Document is required for structural validation");
        }
        
        List<ValidationError> errors = new ArrayList<>();
        
        String messageType = context.getMessageType();
        if (messageType == null || "UNKNOWN".equals(messageType)) {
            logger.debug("Skipping structural validation for unknown message type");
            return errors; // No hay errores, pero tampoco validación
        }
        
        try {
            logger.debug("Starting structural validation for message type: {}", messageType);
            long startTime = System.currentTimeMillis();
            
            Schema schema = getSchemaForMessageType(messageType);
            if (schema != null) {
                validateAgainstSchema(context.getOriginalXml(), schema, errors);
            } else {
                logger.debug("No schema available for message type: {}", messageType);
                // No es un error, simplemente no hay esquema disponible para validar
            }
            
            long validationTime = System.currentTimeMillis() - startTime;
            logger.debug("Structural validation completed in {}ms with {} errors", validationTime, errors.size());
            
        } catch (Exception e) {
            logger.error("Error during structural validation", e);
            errors.add(ValidationError.structuralError("VALIDATION_ERROR", 
                "Internal validation error: " + e.getMessage(), "/"));
        }
        
        return errors;
    }
    
    private Schema getSchemaForMessageType(String messageType) {
        try {
            // Verificar cache primero
            if (schemaCache.containsKey(messageType)) {
                return schemaCache.get(messageType);
            }
            
            String schemaPath = SCHEMA_MAPPINGS.get(messageType);
            if (schemaPath == null) {
                logger.debug("No schema mapping found for message type: {}", messageType);
                return null;
            }
            
            // Intentar cargar el esquema
            Schema schema = loadSchema(schemaPath);
            if (schema != null) {
                schemaCache.put(messageType, schema);
                logger.debug("Loaded and cached schema for message type: {}", messageType);
            }
            
            return schema;
            
        } catch (Exception e) {
            logger.warn("Could not load schema for message type {}: {}", messageType, e.getMessage());
            return null;
        }
    }
    
    private Schema loadSchema(String schemaPath) {
        try {
            ClassPathResource resource = new ClassPathResource(schemaPath);
            if (!resource.exists()) {
                logger.debug("Schema file not found: {}", schemaPath);
                return null;
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                StreamSource source = new StreamSource(inputStream);
                return schemaFactory.newSchema(source);
            }
            
        } catch (SAXException e) {
            logger.warn("Error parsing schema file {}: {}", schemaPath, e.getMessage());
            return null;
        } catch (IOException e) {
            logger.warn("Error reading schema file {}: {}", schemaPath, e.getMessage());
            return null;
        }
    }
    
    private void validateAgainstSchema(String xmlContent, Schema schema, List<ValidationError> errors) {
        try {
            Validator validator = schema.newValidator();
            
            // Configurar error handler personalizado
            SchemaValidationErrorHandler errorHandler = new SchemaValidationErrorHandler(errors);
            validator.setErrorHandler(errorHandler);
            
            // Realizar validación
            ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
            StreamSource source = new StreamSource(inputStream);
            validator.validate(source);
            
        } catch (SAXException e) {
            logger.debug("Schema validation failed: {}", e.getMessage());
            errors.add(ValidationError.structuralError("SCHEMA_VALIDATION_FAILED", 
                "Schema validation failed: " + e.getMessage(), "/"));
        } catch (IOException e) {
            logger.error("I/O error during schema validation", e);
            errors.add(ValidationError.structuralError("IO_ERROR", 
                "I/O error during validation: " + e.getMessage(), "/"));
        }
    }
      @Override
    public boolean canHandle(String messageType) {
        return messageType != null;  // StructuralValidator puede manejar cualquier tipo de mensaje
    }
    
    @Override
    public String getValidationType() {
        return "STRUCTURAL";
    }
    
    /**
     * Error handler personalizado para capturar errores de validación de esquema
     */
    private static class SchemaValidationErrorHandler implements org.xml.sax.ErrorHandler {
        
        private final List<ValidationError> errors;
        
        public SchemaValidationErrorHandler(List<ValidationError> errors) {
            this.errors = errors;
        }
        
        @Override
        public void warning(SAXParseException exception) {
            logger.debug("Schema validation warning: {}", exception.getMessage());
            // Las advertencias no se consideran errores
        }
        
        @Override
        public void error(SAXParseException exception) {
            logger.debug("Schema validation error: {}", exception.getMessage());
            String xpath = createXPath(exception);
            errors.add(ValidationError.structuralError("SCHEMA_ERROR", exception.getMessage(), xpath));
        }
        
        @Override
        public void fatalError(SAXParseException exception) {
            logger.debug("Schema validation fatal error: {}", exception.getMessage());
            String xpath = createXPath(exception);
            errors.add(ValidationError.structuralError("SCHEMA_FATAL_ERROR", exception.getMessage(), xpath));
        }
        
        private String createXPath(SAXParseException exception) {
            int lineNumber = exception.getLineNumber();
            int columnNumber = exception.getColumnNumber();
            
            if (lineNumber > 0) {
                return String.format("/[line:%d, column:%d]", lineNumber, columnNumber);
            }
            
            return "/";
        }
    }
}
