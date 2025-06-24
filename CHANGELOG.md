# Changelog - ISO 20022 Simulator

Todos los cambios notables de este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2025-06-24

### ✨ Added
- Soporte completo para mensajes `pacs.004.001.09` (Payment Return)
- Validación null-safe en `SimpleBusinessRuleValidator` y `StructuralValidator`
- XPath expressions agnósticas de namespace usando `local-name()`
- Validación de atributos de moneda (@Ccy) en documentos XML
- Detección mejorada de elementos raíz `PmtRtr` en parser XML
- Logging detallado para debugging de validaciones
- Release notes automáticas y documentación de cambios

### 🔧 Fixed
- **CRÍTICO**: Validadores no detectaban errores en mensajes con namespaces por defecto
- **CRÍTICO**: Parser retornaba "unknown" para mensajes `pacs.004.001.09` válidos
- Manejo incorrecto de contextos nulos en validadores
- Error de inicialización SQL en tests por archivo `test-data.sql` faltante
- Configuración duplicada de perfiles Spring en `application-test.yml`
- Validaciones de MsgId, Currency, Amount y BIC que fallaban silenciosamente
- XPath queries que no funcionaban con documentos XML namespaciados

### 🚀 Changed
- Migración de XPath específico de namespace a approach agnóstico
- Mejora en la lógica de fallback para detección de tipos de mensaje
- Optimización de configuración de testing para mejor rendimiento
- Actualización de versión de 1.0.0-SNAPSHOT a 1.1.0 (release estable)

### 🧪 Testing
- **53 tests** ejecutándose correctamente (anteriormente: múltiples fallos)
- Cobertura de código mejorada en validadores
- Tests de integración end-to-end funcionando al 100%
- Suite completa de tests de reglas de negocio validada

### 📋 Technical Debt
- Limpieza de imports no utilizados en validadores
- Refactoring de métodos de validación para mejor legibilidad
- Eliminación de código comentado y configuraciones obsoletas

## [1.0.0] - 2025-06-20

### ✨ Added
- Implementación inicial del simulador ISO 20022
- Soporte para mensajes `pacs.008`, `pain.001`, `camt.053`
- Validación estructural básica usando XSD schemas
- Validación de reglas de negocio para elementos comunes
- API REST reactiva con Spring WebFlux
- Base de datos H2 para testing y PostgreSQL para producción
- Métricas con Micrometer y Prometheus
- Documentación OpenAPI/Swagger
- Configuración Docker y Docker Compose
- Pipeline CI/CD básico
- Logging estructurado con Logstash

### 🏗️ Infrastructure
- Maven build system con perfiles multi-ambiente
- Spring Boot 3.1.12 como framework base
- Java 17 como versión mínima requerida
- JaCoCo para cobertura de código
- Testcontainers para testing de integración

### 📚 Documentation
- README.md con instrucciones de instalación
- Documentación de arquitectura
- Guías de desarrollo y contribución
- Documentación de API

---

## Tipos de Cambios

- `Added` para nuevas características
- `Changed` para cambios en funcionalidad existente
- `Deprecated` para características que serán removidas
- `Removed` para características removidas
- `Fixed` para corrección de bugs
- `Security` para vulnerabilidades corregidas

## Versionado

Este proyecto sigue [Semantic Versioning](https://semver.org/):

- **MAJOR** (X.0.0): Cambios incompatibles en la API
- **MINOR** (0.X.0): Nueva funcionalidad compatible hacia atrás
- **PATCH** (0.0.X): Correcciones de bugs compatibles hacia atrás
