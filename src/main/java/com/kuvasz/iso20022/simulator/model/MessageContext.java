package com.kuvasz.iso20022.simulator.model;

import org.w3c.dom.Document;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Representa un mensaje ISO 20022 parseado
 */
public class MessageContext {
    
    private String messageId;
    private String messageType;
    private String businessMessageIdentifier;
    private String messageDefinitionIdentifier;
    private LocalDateTime creationDateTime;
    private String senderId;
    private String receiverId;
    private String originalXml;
    private Object parsedMessage;
    private Document parsedDocument;
    private Map<String, Object> properties;
    
    public MessageContext() {
        this.creationDateTime = LocalDateTime.now();
    }
    
    public MessageContext(String messageId, String messageType) {
        this();
        this.messageId = messageId;
        this.messageType = messageType;
    }
    
    // Getters and Setters
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
    
    public String getBusinessMessageIdentifier() {
        return businessMessageIdentifier;
    }
    
    public void setBusinessMessageIdentifier(String businessMessageIdentifier) {
        this.businessMessageIdentifier = businessMessageIdentifier;
    }
    
    public String getMessageDefinitionIdentifier() {
        return messageDefinitionIdentifier;
    }
    
    public void setMessageDefinitionIdentifier(String messageDefinitionIdentifier) {
        this.messageDefinitionIdentifier = messageDefinitionIdentifier;
    }
    
    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }
    
    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    
    public String getReceiverId() {
        return receiverId;
    }
    
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
    
    public String getOriginalXml() {
        return originalXml;
    }
    
    public void setOriginalXml(String originalXml) {
        this.originalXml = originalXml;
    }
    
    public Object getParsedMessage() {
        return parsedMessage;
    }
      public void setParsedMessage(Object parsedMessage) {
        this.parsedMessage = parsedMessage;
    }
    
    public Document getParsedDocument() {
        return parsedDocument;
    }
    
    public void setParsedDocument(Document parsedDocument) {
        this.parsedDocument = parsedDocument;
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
    public void addProperty(String key, Object value) {
        if (properties == null) {
            properties = new java.util.HashMap<>();
        }
        properties.put(key, value);
    }
    
    public Object getProperty(String key) {
        return properties != null ? properties.get(key) : null;
    }
    
    @Override
    public String toString() {
        return String.format("MessageContext{messageId='%s', messageType='%s', senderId='%s', receiverId='%s'}", 
            messageId, messageType, senderId, receiverId);
    }
}
