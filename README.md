# ISO 20022 Simulator

[![CI/CD Pipeline](https://github.com/sebastian-alejandro/ISO20022_SIM/actions/workflows/ci.yml/badge.svg)](https://github.com/sebastian-alejandro/ISO20022_SIM/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.12-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-enabled-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version](https://img.shields.io/badge/Version-1.1.1-success.svg)](./CHANGELOG.md)
[![Tests](https://img.shields.io/badge/Tests-53%20passing-brightgreen.svg)](./RELEASE_NOTES_v1.1.0.md)

Simulador para mensajes financieros ISO 20022 desarrollado en Java con Spring Boot, diseñado para soportar 1000 TPS con comunicación HTTP/TCP-IP, logging estructurado y métricas de monitoreo.

## 🎯 **Última Versión: v1.1.1**

### ✨ **Corrección Crítica en v1.1.1**
- 🚨 **CI/CD Fix**: GitHub Actions pipeline completamente funcional
- ✅ **Maven Wrapper**: Regenerado correctamente con permisos apropiados
- ✅ **Testing**: Pipeline de CI/CD ejecutando tests automáticamente
- ✅ **Reports**: Generación de reportes de pruebas habilitada

### ✨ **Novedades en v1.1.0**
- ✅ **Validación robusta**: Todos los validadores funcionando al 100%
- ✅ **Soporte completo pacs.004**: Mensajes Payment Return totalmente soportados
- ✅ **53 tests pasando**: Suite completa de testing validada
- ✅ **XPath mejorado**: Manejo robusto de namespaces XML
- ✅ **Null-safe validation**: Manejo seguro de contextos nulos

📋 [Ver Release Notes v1.1.1](./RELEASE_NOTES_v1.1.1.md) | [Ver v1.1.0](./RELEASE_NOTES_v1.1.0.md) | 📜 [Changelog](./CHANGELOG.md)

## 🚀 Quick Start

### Prerrequisitos
- Java 17 LTS
- Maven 3.9+
- Docker y Docker Compose
- PostgreSQL 16 (para producción)

### Desarrollo Local

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/sebastian-alejandro/ISO20022_SIM.git
   cd ISO20022_SIM
   ```

2. **Iniciar base de datos de desarrollo**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

3. **Ejecutar la aplicación**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

4. **Verificar funcionamiento**
   ```bash
   curl http://localhost:8080/health
   ```

### Producción

1. **Construir imagen Docker**
   ```bash
   docker-compose build
   ```

2. **Iniciar todos los servicios**
   ```bash
   docker-compose up -d
   ```

## 📊 Endpoints Principales

| Endpoint | Método | Descripción | Content-Type |
|----------|--------|-------------|--------------|
| `/health` | GET | Health check | application/json |
| `/api/v1/info` | GET | Información de la API | application/json |
| `/api/v1/iso20022/process` | POST | Procesar mensaje ISO 20022 | application/xml |
| `/actuator/metrics` | GET | Métricas de la aplicación | application/json |
| `/actuator/prometheus` | GET | Métricas para Prometheus | text/plain |

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────┐
│                Frontend/Client              │
└─────────────────┬───────────────────────────┘
                  │ HTTP/TCP
┌─────────────────▼───────────────────────────┐
│              Load Balancer                  │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│           ISO 20022 Simulator               │
│  ┌─────────────────────────────────────┐   │
│  │         WebFlux Controllers         │   │
│  └─────────────────┬───────────────────┘   │
│  ┌─────────────────▼───────────────────┐   │
│  │       Message Processing Service     │   │
│  └─────────────────┬───────────────────┘   │
│  ┌─────────────────▼───────────────────┐   │
│  │     Parser/Validator/Generator      │   │
│  └─────────────────┬───────────────────┘   │
└─────────────────────┼───────────────────────┘
                      │
┌─────────────────────▼───────────────────────┐
│              PostgreSQL Database            │
└─────────────────────────────────────────────┘
```

## 🔧 Configuración

### Profiles Disponibles
- **dev**: Desarrollo local con H2 en memoria
- **test**: Testing con H2 en memoria
- **prod**: Producción con PostgreSQL

### Variables de Entorno (Producción)
```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=iso20022_sim
DB_USERNAME=postgres
DB_PASSWORD=your_password
SERVER_PORT=8080
JAVA_OPTS="-Xms512m -Xmx1024m"
```

### Configuración del Simulador
```yaml
simulator:
  performance:
    max-concurrent-requests: 1000
    request-timeout: PT30S
    thread-pool-size: 50
  iso20022:
    validate-schema: true
    supported-messages:
      - pain.001
      - pacs.008
      - camt.056
```

## 🧪 Testing

### Tests Unitarios
```bash
./mvnw test
```

### Tests de Integración
```bash
./mvnw failsafe:integration-test
```

### Tests con Coverage
```bash
./mvnw clean test jacoco:report
open target/site/jacoco/index.html
```

### Load Testing
```bash
# Instalar JMeter
# Ejecutar plan de pruebas incluido
jmeter -n -t src/test/jmeter/load-test.jmx -l results.jtl
```

## 📈 Monitoreo

### Métricas Disponibles
- **TPS (Transacciones por segundo)**
- **Latencia de respuesta (P50, P95, P99)**
- **Conexiones activas**
- **Errores por tipo**
- **Métricas de JVM**

### Dashboards
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Spring Boot Actuator**: http://localhost:8080/actuator

## 🔒 Seguridad

### Características Implementadas
- Input validation y sanitization
- Rate limiting
- Structured logging para auditoría
- Health checks seguros
- Container non-root execution

### Configuración SSL/TLS
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: password
    key-store-type: PKCS12
```

## 📚 Desarrollo

### Estructura del Proyecto
```
src/
├── main/java/com/kuvasz/iso20022/simulator/
│   ├── config/          # Configuraciones
│   ├── controller/      # REST Controllers
│   ├── service/         # Lógica de negocio
│   ├── repository/      # Acceso a datos
│   ├── model/          # Entidades y DTOs
│   ├── parser/         # Parsers ISO 20022
│   ├── validator/      # Validadores
│   ├── exception/      # Exception handlers
│   └── util/           # Utilidades
└── test/               # Tests unitarios e integración
```

### Convenciones de Código
- Java Code Style: Google Java Style Guide
- Naming: CamelCase para clases, camelCase para métodos
- Documentación: JavaDoc obligatorio para APIs públicas
- Testing: Cobertura mínima del 80%

## 🚀 Deployment

### Kubernetes
```bash
# Aplicar manifests
kubectl apply -f k8s/
```

### Docker Swarm
```bash
# Deploy stack
docker stack deploy -c docker-compose.yml iso20022
```

## 🐛 Troubleshooting

### Problemas Comunes

1. **OutOfMemoryError**
   - Aumentar heap size: `-Xmx2048m`
   - Revisar métricas de memoria en Grafana

2. **Connection Pool Exhausted**
   - Aumentar `simulator.database.connection-pool-size`
   - Revisar queries lentas en logs

3. **High Latency**
   - Verificar thread pool configuration
   - Revisar métricas de garbage collection

### Logs
```bash
# Ver logs en tiempo real
docker-compose logs -f iso20022-simulator

# Buscar errores específicos
grep "ERROR" logs/iso20022-simulator.log
```

## 📄 Licencia

Copyright © 2025 Kuvasz Solutions. Todos los derechos reservados.

## 👥 Equipo de Desarrollo

- **Tech Lead**: [Nombre]
- **Backend Developers**: [Nombres]
- **DevOps Engineer**: [Nombre]
- **QA Engineer**: [Nombre]

## 📞 Soporte

Para soporte técnico, contactar:
- Email: support@kuvasz.com
- Slack: #iso20022-simulator
- Issues: GitHub Issues
