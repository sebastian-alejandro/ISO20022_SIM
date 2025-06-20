# Documentación Técnica - Componentes Core ISO 20022

## Arquitectura General

El simulador ISO 20022 está diseñado con una arquitectura modular que separa las responsabilidades en componentes especializados:

```
┌─────────────────────────────────────────────────────────────┐
│                    ISO 20022 Simulator                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Message Processor                       │
│  ┌───────────────┐ ┌───────────────┐ ┌───────────────────┐  │
│  │    Parser     │ │   Validator   │ │   Generator       │  │
│  │               │ │               │ │                   │  │
│  │ - XML Parsing │ │ - Structural  │ │ - Response Gen    │  │
│  │ - Context     │ │ - Business    │ │ - Status Reports  │  │
│  │   Creation    │ │   Rules       │ │ - Error Messages  │  │
│  └───────────────┘ └───────────────┘ └───────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Componentes Core

### 1. Parser (com.kuvasz.iso20022.simulator.core.parser)

#### MessageParser Interface
```java
public interface MessageParser {
    MessageContext parse(String xmlContent) throws ParsingException;
    boolean canHandle(String messageType);
    String getParserType();
}
```

#### GenericXMLParser Implementation
- **Propósito**: Parser DOM genérico para mensajes ISO 20022
- **Tecnología**: DOM API de Java
- **Características**:
  - Parsing namespace-aware
  - Extracción automática de metadatos
  - Manejo robusto de errores
  - Cache de documentos parseados

**Uso**:
```java
MessageParser parser = new GenericXMLParser();
MessageContext context = parser.parse(xmlContent);
```

### 2. Validadores (com.kuvasz.iso20022.simulator.core.validator)

#### MessageValidator Interface
```java
public interface MessageValidator {
    List<ValidationError> validate(MessageContext context) throws ValidationException;
    boolean canHandle(String messageType);
    String getValidationType();
}
```

#### StructuralValidator Implementation
- **Propósito**: Validación estructural con esquemas XSD
- **Tecnología**: Xerces XML Parser
- **Características**:
  - Cache de esquemas para performance
  - Validación contra XSD ISO 20022
  - Reporting detallado de errores
  - Soporte para múltiples versiones de esquemas

**Configuración**:
```java
// Los esquemas XSD deben estar en src/main/resources/schemas/
// Estructura esperada:
// schemas/
//   ├── pain.001.001.03.xsd
//   ├── pacs.008.001.02.xsd
//   └── camt.053.001.02.xsd
```

#### BusinessRuleValidator Implementation
- **Propósito**: Validación de reglas de negocio específicas ISO 20022
- **Características**:
  - Validación de fechas ISO 8601
  - Validación de códigos BIC
  - Validación de formatos de montos
  - Validación de códigos de moneda ISO 4217
  - Reglas específicas por tipo de mensaje

**Reglas Implementadas**:
- **Fechas**: Formato ISO 8601 obligatorio
- **BIC**: Patrón `^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$`
- **Montos**: Patrón `^\\d{1,18}(\\.\\d{1,5})?$`, valores > 0
- **Monedas**: Lista de códigos ISO 4217 válidos

### 3. Generador de Respuestas (com.kuvasz.iso20022.simulator.core.generator)

#### ResponseGenerator Interface
```java
public interface ResponseGenerator {
    String generateResponse(MessageContext originalContext, ProcessingResult processingResult);
    boolean canHandle(String messageType);
    String getResponseType();
}
```

#### ISO20022ResponseGenerator Implementation
- **Propósito**: Generación de respuestas XML conformes ISO 20022
- **Características**:
  - Respuestas específicas por tipo de mensaje
  - Manejo de estados de éxito/error
  - Generación de IDs únicos
  - Formateo XML conforme a estándares

**Tipos de Respuesta Soportados**:
- `pacs.008.001.08` → `pacs.002.001.10` (Payment Status Report)
- `pain.001.001.03` → `pain.002.001.03` (Customer Payment Status Report)
- `pacs.004.001.02` → `pacs.002.001.10` (Payment Return Status)

## Modelos de Datos

### MessageContext
```java
public class MessageContext {
    private String messageId;        // ID único del mensaje
    private String messageType;      // Tipo de mensaje (ej: pain.001.001.03)
    private String messageName;      // Nombre del elemento raíz
    private String senderId;         // ID del remitente
    private String receiverId;       // ID del destinatario
    private String rawContent;       // Contenido XML original
    private Document parsedDocument; // Documento DOM parseado
    private Map<String, String> namespaces; // Namespaces del XML
    private LocalDateTime timestamp; // Timestamp de procesamiento
}
```

### ProcessingResult
```java
public class ProcessingResult {
    public enum Status { SUCCESS, WARNING, ERROR }
    
    private Status status;
    private String message;
    private List<ValidationError> errors;
    private Map<String, Object> metadata;
    private LocalDateTime processedAt;
}
```

### ValidationError
```java
public class ValidationError {
    public enum ErrorType { 
        STRUCTURAL, BUSINESS_RULE, FORMAT, 
        MISSING_FIELD, INVALID_VALUE, SCHEMA_VIOLATION 
    }
    
    private ErrorType type;
    private String code;
    private String message;
    private String field;
    private String xpath;
    private Object actualValue;
    private Object expectedValue;
}
```

## Manejo de Excepciones

### Jerarquía de Excepciones
```
ISO20022Exception (base)
├── ParsingException
└── ValidationException
```

### ISO20022Exception
- **Propósito**: Excepción base para todos los errores del simulador
- **Características**:
  - Código de error específico
  - Mensaje descriptivo
  - ID del mensaje (opcional)
  - Timestamp automático

### ParsingException
- **Uso**: Errores durante el parsing XML
- **Causas Comunes**:
  - XML mal formado
  - Encoding inválido
  - Contenido vacío o nulo

### ValidationException
- **Uso**: Errores durante la validación
- **Características**:
  - Lista de errores de validación
  - Soporte para múltiples errores
  - Contexto detallado del error

## Configuración y Uso

### Dependencias Maven
```xml
<dependencies>
    <!-- ISO 20022 Binding -->
    <dependency>
        <groupId>com.prowidesoftware</groupId>
        <artifactId>pw-iso20022</artifactId>
        <version>SRU2023-10.1.9</version>
    </dependency>
    
    <!-- XML Validation -->
    <dependency>
        <groupId>xerces</groupId>
        <artifactId>xercesImpl</artifactId>
        <version>2.12.2</version>
    </dependency>
    
    <!-- XML Testing -->
    <dependency>
        <groupId>org.xmlunit</groupId>
        <artifactId>xmlunit-core</artifactId>
        <version>2.9.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Configuración Spring
```java
@Configuration
public class ISO20022Config {
    
    @Bean
    public MessageParser messageParser() {
        return new GenericXMLParser();
    }
    
    @Bean
    public List<MessageValidator> messageValidators() {
        return Arrays.asList(
            new StructuralValidator(),
            new BusinessRuleValidator()
        );
    }
    
    @Bean
    public ResponseGenerator responseGenerator() {
        return new ISO20022ResponseGenerator();
    }
}
```

### Ejemplo de Uso Completo
```java
@Service
public class MessageProcessingService {
    
    @Autowired
    private MessageParser parser;
    
    @Autowired
    private List<MessageValidator> validators;
    
    @Autowired
    private ResponseGenerator responseGenerator;
    
    public String processMessage(String xmlContent) {
        try {
            // 1. Parse del mensaje
            MessageContext context = parser.parse(xmlContent);
            
            // 2. Validación
            List<ValidationError> allErrors = new ArrayList<>();
            for (MessageValidator validator : validators) {
                if (validator.canHandle(context.getMessageType())) {
                    allErrors.addAll(validator.validate(context));
                }
            }
            
            // 3. Creación del resultado
            ProcessingResult result = new ProcessingResult();
            if (allErrors.isEmpty()) {
                result.setStatus(ProcessingResult.Status.SUCCESS);
                result.setMessage("Mensaje procesado exitosamente");
            } else {
                result.setStatus(ProcessingResult.Status.ERROR);
                result.setErrors(allErrors);
                result.setMessage("Errores de validación encontrados");
            }
            
            // 4. Generación de respuesta
            return responseGenerator.generateResponse(context, result);
            
        } catch (Exception e) {
            // Manejo de errores
            ProcessingResult errorResult = new ProcessingResult();
            errorResult.setStatus(ProcessingResult.Status.ERROR);
            errorResult.setMessage("Error procesando mensaje: " + e.getMessage());
            
            return responseGenerator.generateResponse(null, errorResult);
        }
    }
}
```

## Testing

### Tests Unitarios
- **GenericXMLParserTest**: Tests para parsing XML
- **StructuralValidatorTest**: Tests para validación XSD
- **BusinessRuleValidatorTest**: Tests para reglas de negocio
- **ISO20022ResponseGeneratorTest**: Tests para generación de respuestas

### Tests de Integración
- **ISO20022ProcessingIntegrationTest**: Tests end-to-end

### Datos de Test
```java
// XML de prueba para pain.001.001.03
String testXML = """
    <?xml version="1.0" encoding="UTF-8"?>
    <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.03">
        <CstmrCdtTrfInitn>
            <GrpHdr>
                <MsgId>TEST-MSG-001</MsgId>
                <CreDtTm>2023-12-20T10:30:00Z</CreDtTm>
                <NbOfTxs>1</NbOfTxs>
            </GrpHdr>
        </CstmrCdtTrfInitn>
    </Document>
    """;
```

## Performance y Optimización

### Cache de Esquemas XSD
- Los esquemas se cargan una sola vez y se cachean en memoria
- Mejora significativa en performance para validaciones repetidas

### Pool de Parsers
- Uso de factory pattern para crear parsers
- Reutilización de objetos DocumentBuilder

### Logging
- Logging estructurado con SLF4J
- Niveles apropiados (DEBUG, INFO, WARN, ERROR)
- Contexto de mensaje en logs

## Troubleshooting

### Problemas Comunes

1. **Error de Namespace**
   - Causa: XML sin namespace o namespace incorrecto
   - Solución: Verificar que el XML tenga el namespace ISO 20022 correcto

2. **Esquema XSD No Encontrado**
   - Causa: Archivo XSD faltante en resources/schemas
   - Solución: Agregar el archivo XSD correspondiente

3. **Error de Validación BIC**
   - Causa: Formato de BIC incorrecto
   - Solución: Usar formato válido: `ABCDUS33XXX`

4. **Error de Formato de Fecha**
   - Causa: Fecha no en formato ISO 8601
   - Solución: Usar formato: `2023-12-20T10:30:00Z`

### Logs de Debug
```java
// Habilitar logs de debug en application.properties
logging.level.com.kuvasz.iso20022.simulator.core=DEBUG
```

## Roadmap Futuro

### Sprint 2
- Integración con esquemas XSD oficiales ISO 20022
- Mejoras en performance del parser
- Soporte para más tipos de mensaje

### Sprint 3
- Validaciones de negocio más sofisticadas
- Soporte para múltiples versiones de esquemas
- Métricas y monitoreo

### Sprint 4
- API REST para procesamiento
- Interfaz web para testing
- Documentación interactiva
