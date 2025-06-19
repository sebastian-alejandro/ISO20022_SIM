package com.kuvasz.iso20022.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Aplicación principal del Simulador ISO 20022
 * 
 * Simulador para mensajes financieros ISO 20022 con soporte para:
 * - 1000 TPS (Transacciones por segundo)
 * - Comunicación HTTP/TCP-IP
 * - Logging estructurado para monitoreo
 * - Métricas de performance
 * 
 * @author Kuvasz Solutions
 * @version 1.0.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class Iso20022SimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(Iso20022SimulatorApplication.class, args);
    }
}
