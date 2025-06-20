package com.kuvasz.iso20022.simulator.core.validator;

import com.kuvasz.iso20022.simulator.exception.ValidationException;
import com.kuvasz.iso20022.simulator.model.MessageContext;
import com.kuvasz.iso20022.simulator.model.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validador de reglas de negocio para mensajes ISO 20022.
 * Implementa validaciones específicas del dominio financiero según los estándares ISO 20022.
 */
@Component
public class SimpleBusinessRuleValidator implements MessageValidator {

    private static final Logger logger = LoggerFactory.getLogger(SimpleBusinessRuleValidator.class);
    
    private final XPath xpath = XPathFactory.newInstance().newXPath();
    
    // Patrones de validación
    private static final Pattern BIC_PATTERN = Pattern.compile("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d{1,18}(\\.\\d{1,5})?$");
    
    // Códigos de moneda ISO 4217 más comunes
    private static final List<String> VALID_CURRENCIES = List.of(
        "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "SEK", "NZD",
        "MXN", "SGD", "HKD", "NOK", "TRY", "RUB", "INR", "BRL", "ZAR", "KRW"
    );

    @Override
    public List<ValidationError> validate(MessageContext context) throws ValidationException {
        logger.debug("Iniciando validación de reglas de negocio para mensaje: {}", context.getMessageId());
        
        List<ValidationError> errors = new ArrayList<>();
        String messageType = context.getMessageType();
        Document document = context.getParsedDocument();
        
        if (document == null) {
            throw new ValidationException("Documento XML no disponible para validación de reglas de negocio");
        }
        
        try {
            // Validaciones comunes para todos los tipos de mensaje
            validateMessageIdentification(document, errors);
            validateAmounts(document, errors);
            validateCurrencies(document, errors);
            validateBICCodes(document, errors);
            
            // Validaciones específicas por tipo de mensaje
            if (messageType.startsWith("pacs.008") || messageType.startsWith("pain.001")) {
                validatePaymentInstructions(document, errors);
            }
            
            logger.debug("Validación de reglas de negocio completada. Errores encontrados: {}", errors.size());
            return errors;
            
        } catch (Exception e) {
            logger.error("Error durante la validación de reglas de negocio", e);
            throw new ValidationException("Error en validación de reglas de negocio: " + e.getMessage());
        }
    }

    @Override
    public boolean canHandle(String messageType) {
        return messageType != null && (
            messageType.startsWith("pacs.008") ||
            messageType.startsWith("pacs.004") ||
            messageType.startsWith("camt.053") ||
            messageType.startsWith("pain.001")
        );
    }

    @Override
    public String getValidationType() {
        return "BUSINESS_RULES";
    }

    private void validateMessageIdentification(Document document, List<ValidationError> errors) {
        try {
            NodeList msgIdNodes = (NodeList) xpath.evaluate("//MsgId", document, XPathConstants.NODESET);
            
            for (int i = 0; i < msgIdNodes.getLength(); i++) {
                String msgId = msgIdNodes.item(i).getTextContent();
                
                if (msgId == null || msgId.trim().isEmpty()) {
                    errors.add(ValidationError.missingFieldError("MsgId"));
                } else if (msgId.length() > 35) {
                    errors.add(ValidationError.businessRuleError("INVALID_MESSAGE_ID_LENGTH", 
                        "Identificación del mensaje no puede exceder 35 caracteres", "MsgId"));
                }
            }
        } catch (Exception e) {
            errors.add(ValidationError.businessRuleError("MESSAGE_ID_VALIDATION_ERROR", 
                "Error validando identificación del mensaje: " + e.getMessage(), "MsgId"));
        }
    }

    private void validateAmounts(Document document, List<ValidationError> errors) {
        try {
            NodeList amountNodes = (NodeList) xpath.evaluate("//Amt | //InstdAmt | //EqvtAmt", 
                document, XPathConstants.NODESET);
            
            for (int i = 0; i < amountNodes.getLength(); i++) {
                String amount = amountNodes.item(i).getTextContent();
                
                if (amount != null && !amount.trim().isEmpty()) {
                    if (!AMOUNT_PATTERN.matcher(amount).matches()) {
                        errors.add(ValidationError.formatError("INVALID_AMOUNT_FORMAT", 
                            "Formato de monto inválido", amountNodes.item(i).getLocalName(), amount));
                    } else {
                        try {
                            BigDecimal amountValue = new BigDecimal(amount);
                            if (amountValue.compareTo(BigDecimal.ZERO) <= 0) {
                                errors.add(ValidationError.businessRuleError("INVALID_AMOUNT_VALUE", 
                                    "El monto debe ser mayor que cero", amountNodes.item(i).getLocalName()));
                            }
                        } catch (NumberFormatException e) {
                            errors.add(ValidationError.formatError("INVALID_AMOUNT_NUMBER", 
                                "Monto no es un número válido", amountNodes.item(i).getLocalName(), amount));
                        }
                    }
                }
            }
        } catch (Exception e) {
            errors.add(ValidationError.businessRuleError("AMOUNT_VALIDATION_ERROR", 
                "Error validando montos: " + e.getMessage(), "Amount"));
        }
    }

    private void validateCurrencies(Document document, List<ValidationError> errors) {
        try {
            NodeList currencyNodes = (NodeList) xpath.evaluate("//Ccy", document, XPathConstants.NODESET);
            
            for (int i = 0; i < currencyNodes.getLength(); i++) {
                String currency = currencyNodes.item(i).getTextContent();
                
                if (currency != null && !currency.trim().isEmpty()) {
                    if (!VALID_CURRENCIES.contains(currency.toUpperCase())) {
                        errors.add(ValidationError.businessRuleError("INVALID_CURRENCY_CODE", 
                            "Código de moneda no válido: " + currency, "Ccy"));
                    }
                }
            }
        } catch (Exception e) {
            errors.add(ValidationError.businessRuleError("CURRENCY_VALIDATION_ERROR", 
                "Error validando códigos de moneda: " + e.getMessage(), "Ccy"));
        }
    }

    private void validateBICCodes(Document document, List<ValidationError> errors) {
        try {
            NodeList bicNodes = (NodeList) xpath.evaluate("//*[contains(local-name(), 'BIC') or contains(local-name(), 'BICFI')]", 
                document, XPathConstants.NODESET);
            
            for (int i = 0; i < bicNodes.getLength(); i++) {
                String bic = bicNodes.item(i).getTextContent();
                
                if (bic != null && !bic.trim().isEmpty()) {
                    if (!BIC_PATTERN.matcher(bic).matches()) {
                        errors.add(ValidationError.formatError("INVALID_BIC_FORMAT", 
                            "Código BIC inválido", bicNodes.item(i).getLocalName(), bic));
                    }
                }
            }
        } catch (Exception e) {
            errors.add(ValidationError.businessRuleError("BIC_VALIDATION_ERROR", 
                "Error validando códigos BIC: " + e.getMessage(), "BIC"));
        }
    }

    private void validatePaymentInstructions(Document document, List<ValidationError> errors) {
        try {
            NodeList paymentInfoNodes = (NodeList) xpath.evaluate("//PmtInf", document, XPathConstants.NODESET);
            
            if (paymentInfoNodes.getLength() == 0) {
                errors.add(ValidationError.missingFieldError("PmtInf"));
            }
            
        } catch (Exception e) {
            errors.add(ValidationError.businessRuleError("PAYMENT_INSTRUCTION_VALIDATION_ERROR", 
                "Error validando instrucciones de pago: " + e.getMessage(), "PmtInf"));
        }
    }
}
