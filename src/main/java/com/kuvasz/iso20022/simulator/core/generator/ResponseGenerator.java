package com.kuvasz.iso20022.simulator.core.generator;

import com.kuvasz.iso20022.simulator.model.MessageContext;
import com.kuvasz.iso20022.simulator.model.ProcessingResult;

/**
 * Interfaz para generadores de respuestas XML ISO 20022
 */
public interface ResponseGenerator {
    
    /**
     * Genera una respuesta XML basada en el resultado del procesamiento
     * 
     * @param originalContext el contexto del mensaje original
     * @param processingResult el resultado del procesamiento
     * @return el XML de respuesta generado
     */
    String generateResponse(MessageContext originalContext, ProcessingResult processingResult);
    
    /**
     * Verifica si este generador puede crear respuestas para el tipo de mensaje dado
     * 
     * @param messageType el tipo de mensaje ISO 20022
     * @return true si puede generar respuestas, false en caso contrario
     */
    boolean canHandle(String messageType);
    
    /**
     * Obtiene el tipo de respuesta que genera este generador
     * 
     * @return el tipo de respuesta
     */
    String getResponseType();
}
