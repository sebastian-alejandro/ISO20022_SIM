package com.kuvasz.iso20022.simulator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Set;

/**
 * Propiedades de configuración del simulador ISO 20022
 */
@ConfigurationProperties(prefix = "simulator")
@Validated
public class SimulatorProperties {
    
    @Valid
    private Performance performance = new Performance();
    
    @Valid
    private Iso20022 iso20022 = new Iso20022();
    
    @Valid
    private Database database = new Database();
    
    // Getters y Setters
    public Performance getPerformance() { return performance; }
    public void setPerformance(Performance performance) { this.performance = performance; }
    
    public Iso20022 getIso20022() { return iso20022; }
    public void setIso20022(Iso20022 iso20022) { this.iso20022 = iso20022; }
    
    public Database getDatabase() { return database; }
    public void setDatabase(Database database) { this.database = database; }
    
    /**
     * Configuración de performance y concurrencia
     */
    public static class Performance {
        @Min(1)
        private int maxConcurrentRequests = 1000;
        
        @NotNull
        private Duration requestTimeout = Duration.ofSeconds(30);
        
        @Min(1)
        private int threadPoolSize = 50;
        
        @Min(1)
        private int queueCapacity = 1000;
        
        // Getters y Setters
        public int getMaxConcurrentRequests() { return maxConcurrentRequests; }
        public void setMaxConcurrentRequests(int maxConcurrentRequests) { this.maxConcurrentRequests = maxConcurrentRequests; }
        
        public Duration getRequestTimeout() { return requestTimeout; }
        public void setRequestTimeout(Duration requestTimeout) { this.requestTimeout = requestTimeout; }
        
        public int getThreadPoolSize() { return threadPoolSize; }
        public void setThreadPoolSize(int threadPoolSize) { this.threadPoolSize = threadPoolSize; }
        
        public int getQueueCapacity() { return queueCapacity; }
        public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
    }
    
    /**
     * Configuración específica de ISO 20022
     */
    public static class Iso20022 {
        @NotNull
        private String schemaPath = "classpath:schemas/";
        
        private boolean validateSchema = true;
        private boolean enableStrictValidation = false;
        
        @NotNull
        private Set<String> supportedMessages = Set.of("pain.001", "pacs.008", "camt.056");
        
        // Getters y Setters
        public String getSchemaPath() { return schemaPath; }
        public void setSchemaPath(String schemaPath) { this.schemaPath = schemaPath; }
        
        public boolean isValidateSchema() { return validateSchema; }
        public void setValidateSchema(boolean validateSchema) { this.validateSchema = validateSchema; }
        
        public boolean isEnableStrictValidation() { return enableStrictValidation; }
        public void setEnableStrictValidation(boolean enableStrictValidation) { this.enableStrictValidation = enableStrictValidation; }
        
        public Set<String> getSupportedMessages() { return supportedMessages; }
        public void setSupportedMessages(Set<String> supportedMessages) { this.supportedMessages = supportedMessages; }
    }
    
    /**
     * Configuración de base de datos
     */
    public static class Database {
        @Min(1)
        private int connectionPoolSize = 20;
        
        @NotNull
        private Duration connectionTimeout = Duration.ofSeconds(30);
        
        @Min(1)
        private int maxLifetime = 1800;
        
        // Getters y Setters
        public int getConnectionPoolSize() { return connectionPoolSize; }
        public void setConnectionPoolSize(int connectionPoolSize) { this.connectionPoolSize = connectionPoolSize; }
        
        public Duration getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(Duration connectionTimeout) { this.connectionTimeout = connectionTimeout; }
        
        public int getMaxLifetime() { return maxLifetime; }
        public void setMaxLifetime(int maxLifetime) { this.maxLifetime = maxLifetime; }
    }
}
