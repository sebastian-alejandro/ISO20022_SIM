# Notas de la Versión v1.0.0

**Fecha de lanzamiento**: 19 de junio de 2025

## 🎉 Primera versión del ISO 20022 Simulator

Esta es la versión inicial del simulador ISO 20022, que incluye una arquitectura completa basada en microservicios y un stack de tecnologías modernas.

## ✨ Nuevas características

### Aplicación principal
- ✅ **Spring Boot 3.1.12** con Java 17
- ✅ **API REST** con endpoints actuator
- ✅ **Logging estructurado** en formato JSON
- ✅ **Health checks** automáticos
- ✅ **Métricas con Micrometer** y endpoint Prometheus

### Base de datos y persistencia
- ✅ **PostgreSQL 16** como base de datos principal
- ✅ **HikariCP** para pool de conexiones optimizado
- ✅ **Spring Data JPA** para acceso a datos
- ✅ **Flyway** para migración de esquemas

### Cache y performance
- ✅ **Redis 7** para caché distribuido
- ✅ Configuración de TTL optimizada

### Monitoreo y observabilidad
- ✅ **Prometheus** para recolección de métricas
- ✅ **Grafana** para visualización de dashboards
- ✅ Métricas JVM, HTTP y aplicación
- ✅ Logs estructurados con correlación IDs

### DevOps y infraestructura
- ✅ **Docker** multi-stage builds optimizados
- ✅ **Docker Compose** para desarrollo local
- ✅ **GitHub Actions** CI/CD pipeline
- ✅ Contenedores con usuario no-root
- ✅ Health checks en todos los servicios

### Administración
- ✅ **PgAdmin** para administración de base de datos
- ✅ Configuración de perfiles (dev, prod, test)
- ✅ Variables de entorno parametrizadas

## 🔧 Configuración técnica

### Puertos utilizados
- **8080**: Aplicación principal
- **5432**: PostgreSQL
- **6379**: Redis
- **3000**: Grafana
- **9090**: Prometheus
- **5050**: PgAdmin

### Perfiles de aplicación
- **dev**: Desarrollo local con base de datos en memoria
- **prod**: Producción con PostgreSQL y Redis externos
- **test**: Pruebas con TestContainers

## 📊 Métricas disponibles

La aplicación expone métricas en el endpoint `/actuator/prometheus` incluyendo:
- Métricas JVM (memoria, GC, threads)
- Métricas HTTP (requests, latencia, códigos de respuesta)
- Métricas de pool de conexiones (HikariCP)
- Métricas de aplicación personalizadas

## 🚀 Cómo usar

### Inicio rápido
```bash
# Clonar repositorio
git clone https://github.com/sebastian-alejandro/ISO20022_SIM.git
cd ISO20022_SIM

# Iniciar todos los servicios
docker-compose up -d

# Verificar estado
docker-compose ps
```

### URLs de acceso
- **Aplicación**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Métricas**: http://localhost:8080/actuator/prometheus
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **PgAdmin**: http://localhost:5050 (admin@admin.com/admin)

## 🔧 Próximos pasos

### Versión 1.1.0 (próxima)
- [ ] Implementación de endpoints ISO 20022
- [ ] Validación de mensajes XML
- [ ] Soporte para múltiples formatos de mensaje
- [ ] API de configuración dinámica

### Versión 1.2.0
- [ ] Interfaz web de administración
- [ ] Dashboards predefinidos en Grafana
- [ ] Alertas automáticas
- [ ] Documentación API con Swagger

## 🐛 Problemas conocidos

- Ninguno reportado en esta versión inicial

## 📋 Requisitos del sistema

### Mínimos
- **RAM**: 2 GB
- **CPU**: 2 cores
- **Disco**: 1 GB libre
- **Docker**: 20.x o superior
- **Docker Compose**: 2.x o superior

### Recomendados
- **RAM**: 4 GB
- **CPU**: 4 cores
- **Disco**: 5 GB libre
- **SSD** para mejor performance

## 👥 Contribuidores

- **Sebastian Alejandro** - Desarrollo inicial

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Ver archivo [LICENSE](LICENSE) para más detalles.
