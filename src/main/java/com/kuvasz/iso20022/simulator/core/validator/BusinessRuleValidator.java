package com.kuvasz.iso20022.simulator.core.validator;

import com.kuvasz.iso20022.simulator.exception.ValidationException;
import com.kuvasz.iso20022.simulator.model.MessageContext;
import com.kuvasz.iso20022.simulator.model.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validador de reglas de negocio específicas ISO 20022
 */
@Component
public class BusinessRuleValidator implements MessageValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(BusinessRuleValidator.class);
    
    private final XPathFactory xPathFactory;
    
    // Patrones de validación
    private static final Pattern BIC_PATTERN = Pattern.compile("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d{1,18}(\\.\\d{1,5})?$");
    
    // Códigos de moneda ISO 4217 más comunes
    private static final List<String> VALID_CURRENCIES = Arrays.asList(
        "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "CNY", "CLP", "MXN", "BRL", "ARS"
    );
    
    public BusinessRuleValidator() {
        this.xPathFactory = XPathFactory.newInstance();
    }
    
    @Override
    public List<ValidationError> validate(MessageContext context) throws ValidationException {
        List<ValidationError> errors = new ArrayList<>();
        
        try {
            Document document = context.getParsedDocument();            if (document == null) {
                errors.add(ValidationError.businessRuleError("DOCUMENT_NULL", "Parsed document is null", "document"));
                return errors;
            }
            
            String messageType = context.getMessageType();
              // Validaciones específicas por tipo de mensaje
            if (messageType != null) {
                String lowerMessageType = messageType.toLowerCase();
                if (lowerMessageType.startsWith("pain")) {
                    validatePainMessage(document, errors);
                } else if (lowerMessageType.startsWith("pacs")) {
                    validatePacsMessage(document, errors);
                } else if (lowerMessageType.startsWith("camt")) {
                    validateCamtMessage(document, errors);
                }
            }
            
            // Validaciones comunes
            validateDates(document, errors);
            validateAmounts(document, errors);
            validateCurrencies(document, errors);
            validateBICCodes(document, errors);
              } catch (Exception e) {
            logger.error("Error during business rule validation", e);
            throw new ValidationException("Business rule validation failed: " + e.getMessage());
        }
        
        return errors;
    }
    
    private void validatePainMessage(Document document, List<ValidationError> errors) {
        // Validaciones específicas para mensajes PAIN (Payment Initiation)
        validateXPathExists(document, "//GrpHdr/MsgId", "PAIN_MSG_ID_MISSING", 
            "Group Header Message ID is required", errors);
        validateXPathExists(document, "//GrpHdr/CreDtTm", "PAIN_CREATION_DATE_MISSING", 
            "Creation Date Time is required", errors);
    }
    
    private void validatePacsMessage(Document document, List<ValidationError> errors) {
        // Validaciones específicas para mensajes PACS (Payment Clearing and Settlement)
        validateXPathExists(document, "//GrpHdr/MsgId", "PACS_MSG_ID_MISSING", 
            "Group Header Message ID is required", errors);
        validateXPathExists(document, "//GrpHdr/CreDtTm", "PACS_CREATION_DATE_MISSING", 
            "Creation Date Time is required", errors);
    }
    
    private void validateCamtMessage(Document document, List<ValidationError> errors) {
        // Validaciones específicas para mensajes CAMT (Cash Management)
        validateXPathExists(document, "//GrpHdr/MsgId", "CAMT_MSG_ID_MISSING", 
            "Group Header Message ID is required", errors);
        validateXPathExists(document, "//GrpHdr/CreDtTm", "CAMT_CREATION_DATE_MISSING", 
            "Creation Date Time is required", errors);
    }    private void validateDates(Document document, List<ValidationError> errors) {
        try {
            XPath xpath = xPathFactory.newXPath();
            // Use more specific XPath that doesn't rely on namespace prefixes
            XPathExpression expr = xpath.compile("//*[local-name()='CreDtTm'] | //*[local-name()='ReqdExctnDt'] | //*[local-name()='IntrBkSttlmDt']");
            NodeList dateNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            
            for (int i = 0; i < dateNodes.getLength(); i++) {
                Node dateNode = dateNodes.item(i);
                String dateValue = dateNode.getTextContent().trim();
                
                if (dateValue.isEmpty()) {
                    errors.add(ValidationError.businessRuleError("MISSING_CREATION_DATETIME", 
                        "Creation date time cannot be empty", 
                        dateNode.getNodeName()));
                } else if (!isValidISO8601Date(dateValue)) {
                    errors.add(ValidationError.formatError("INVALID_CREATION_DATETIME_FORMAT", 
                        "Date must be in ISO 8601 format: " + dateValue, 
                        dateNode.getNodeName(), dateValue));
                }
            }
        } catch (Exception e) {
            logger.warn("Error validating dates: {}", e.getMessage());
        }
    }
      private void validateAmounts(Document document, List<ValidationError> errors) {
        try {
            XPath xpath = xPathFactory.newXPath();
            XPathExpression expr = xpath.compile("//*[local-name()='InstdAmt'] | //*[local-name()='TtlIntrBkSttlmAmt'] | //*[local-name()='Amt']");
            NodeList amountNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            
            for (int i = 0; i < amountNodes.getLength(); i++) {
                Node amountNode = amountNodes.item(i);
                String amountValue = amountNode.getTextContent().trim();
                
                if (!AMOUNT_PATTERN.matcher(amountValue).matches()) {
                    errors.add(ValidationError.formatError("INVALID_AMOUNT_FORMAT", 
                        "Amount format is invalid: " + amountValue, 
                        amountNode.getNodeName(), amountValue));
                }
                
                try {
                    double amount = Double.parseDouble(amountValue);
                    if (amount <= 0) {
                        errors.add(ValidationError.businessRuleError("INVALID_AMOUNT_VALUE", 
                            "Amount must be greater than zero: " + amountValue, 
                            amountNode.getNodeName()));
                    }
                } catch (NumberFormatException e) {
                    errors.add(ValidationError.formatError("INVALID_AMOUNT_NUMBER", 
                        "Amount is not a valid number: " + amountValue, 
                        amountNode.getNodeName(), amountValue));
                }
            }
        } catch (Exception e) {
            logger.warn("Error validating amounts: {}", e.getMessage());
        }
    }
      private void validateCurrencies(Document document, List<ValidationError> errors) {
        try {
            XPath xpath = xPathFactory.newXPath();
            XPathExpression expr = xpath.compile("//@Ccy");
            NodeList currencyNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            
            for (int i = 0; i < currencyNodes.getLength(); i++) {
                Node currencyNode = currencyNodes.item(i);
                String currencyValue = currencyNode.getTextContent().trim();
                
                if (!VALID_CURRENCIES.contains(currencyValue)) {
                    errors.add(ValidationError.businessRuleError("INVALID_VALUE", 
                        "Currency code is not valid: " + currencyValue, 
                        "Currency"));
                }
            }
        } catch (Exception e) {
            logger.warn("Error validating currencies: {}", e.getMessage());
        }
    }
      private void validateBICCodes(Document document, List<ValidationError> errors) {
        try {
            XPath xpath = xPathFactory.newXPath();
            XPathExpression expr = xpath.compile("//*[local-name()='BICFI'] | //*[local-name()='BIC']");
            NodeList bicNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            
            for (int i = 0; i < bicNodes.getLength(); i++) {
                Node bicNode = bicNodes.item(i);
                String bicValue = bicNode.getTextContent().trim();
                
                if (!BIC_PATTERN.matcher(bicValue).matches()) {
                    errors.add(ValidationError.formatError("INVALID_BIC_FORMAT", 
                        "BIC format is invalid: " + bicValue, 
                        bicNode.getNodeName(), bicValue));
                }
            }
        } catch (Exception e) {
            logger.warn("Error validating BIC codes: {}", e.getMessage());
        }
    }
      private void validateXPathExists(Document document, String xpathExpression, 
                                   String errorCode, String errorMessage, List<ValidationError> errors) {
        try {
            XPath xpath = xPathFactory.newXPath();
            // Convert simple xpath to namespace-aware version
            String namespaceAwareXPath = xpathExpression
                .replace("//GrpHdr/MsgId", "//*[local-name()='GrpHdr']/*[local-name()='MsgId']")
                .replace("//GrpHdr/CreDtTm", "//*[local-name()='GrpHdr']/*[local-name()='CreDtTm']");
            
            XPathExpression expr = xpath.compile(namespaceAwareXPath);
            NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            
            if (nodes.getLength() == 0) {
                errors.add(ValidationError.businessRuleError(errorCode, errorMessage, xpathExpression));
            }
        } catch (Exception e) {
            logger.warn("Error validating XPath {}: {}", xpathExpression, e.getMessage());
        }
    }
      private boolean isValidISO8601Date(String dateString) {
        try {
            // If empty, return false
            if (dateString == null || dateString.trim().isEmpty()) {
                return false;
            }
            
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // This should fail validation
            };
            
            String cleanDate = dateString.replaceAll("Z$", "").replaceAll("[+-]\\d{2}:?\\d{2}$", "");
            
            // Check if the input is in invalid format "2023-12-01 10:00:00" (space instead of T)
            if (dateString.contains(" ") && !dateString.contains("T")) {
                return false; // Invalid ISO 8601 format
            }
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    formatter.parse(cleanDate);
                    return true;
                } catch (DateTimeParseException ignored) {
                    // Intentar el siguiente formato
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
      @Override
    public boolean canHandle(String messageType) {
        return messageType != null && (
            messageType.toLowerCase().startsWith("pain") ||
            messageType.toLowerCase().startsWith("pacs") ||
            messageType.toLowerCase().startsWith("camt")
        );
    }
      @Override
    public String getValidationType() {
        return "BUSINESS_RULE";
    }
}
