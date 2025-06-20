package com.kuvasz.iso20022.simulator.core.parser;

import com.kuvasz.iso20022.simulator.exception.ParsingException;
import com.kuvasz.iso20022.simulator.model.MessageContext;

/**
 * Interfaz para parsers de mensajes ISO 20022
 */
public interface MessageParser {
    
    /**
     * Parsea un mensaje XML ISO 20022
     * 
     * @param xmlContent el contenido XML del mensaje
     * @return el contexto del mensaje parseado
     * @throws ParsingException si ocurre un error durante el parsing
     */
    MessageContext parse(String xmlContent) throws ParsingException;
    
    /**
     * Verifica si este parser puede procesar el tipo de mensaje dado
     * 
     * @param messageType el tipo de mensaje ISO 20022
     * @return true si puede procesar el mensaje, false en caso contrario
     */
    boolean canHandle(String messageType);
    
    /**
     * Obtiene el tipo de mensaje que maneja este parser
     * 
     * @return el tipo de mensaje
     */
    String getMessageType();
}
