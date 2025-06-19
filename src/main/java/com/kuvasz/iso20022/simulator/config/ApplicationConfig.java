package com.kuvasz.iso20022.simulator.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Configuración principal de la aplicación
 */
@Configuration
@EnableConfigurationProperties(SimulatorProperties.class)
public class ApplicationConfig {

    /**
     * Configuración de rutas funcionales para WebFlux
     */
    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions
            .route(GET("/health"), this::healthCheck)
            .andRoute(GET("/api/v1/info"), this::apiInfo)
            .andRoute(POST("/api/v1/iso20022/process")
                .and(accept(APPLICATION_XML)), this::processMessage);
    }

    /**
     * Health check endpoint
     */
    private Mono<ServerResponse> healthCheck(org.springframework.web.reactive.function.server.ServerRequest request) {
        return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .bodyValue("{\"status\":\"UP\",\"service\":\"iso20022-simulator\"}");
    }

    /**
     * API info endpoint
     */
    private Mono<ServerResponse> apiInfo(org.springframework.web.reactive.function.server.ServerRequest request) {
        return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .bodyValue("{\"name\":\"ISO 20022 Simulator\",\"version\":\"1.0.0\",\"description\":\"Simulador para mensajes ISO 20022\"}");
    }

    /**
     * Placeholder para procesamiento de mensajes ISO 20022
     */
    private Mono<ServerResponse> processMessage(org.springframework.web.reactive.function.server.ServerRequest request) {
        return ServerResponse.ok()
            .contentType(APPLICATION_XML)
            .bodyValue("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Status>PLACEHOLDER</Status></Response>");
    }
}
