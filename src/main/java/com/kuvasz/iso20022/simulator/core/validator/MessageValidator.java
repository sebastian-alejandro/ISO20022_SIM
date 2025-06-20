package com.kuvasz.iso20022.simulator.core.validator;

import com.kuvasz.iso20022.simulator.exception.ValidationException;
import com.kuvasz.iso20022.simulator.model.MessageContext;
import com.kuvasz.iso20022.simulator.model.ValidationError;

import java.util.List;

/**
 * Interfaz para validadores de mensajes ISO 20022
 */
public interface MessageValidator {
    
    /**
     * Valida un mensaje ISO 20022
     * 
     * @param context el contexto del mensaje a validar
     * @return lista de errores de validación (vacía si no hay errores)
     * @throws ValidationException si ocurre un error durante la validación
     */
    List<ValidationError> validate(MessageContext context) throws ValidationException;
    
    /**
     * Verifica si este validador puede procesar el tipo de mensaje dado
     * 
     * @param messageType el tipo de mensaje ISO 20022
     * @return true si puede validar el mensaje, false en caso contrario
     */
    boolean canHandle(String messageType);
    
    /**
     * Obtiene el tipo de validación que realiza este validador
     * 
     * @return el tipo de validación
     */
    String getValidationType();
}
