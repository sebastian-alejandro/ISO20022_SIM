# ISO 20022 Simulator - Resumen de Servicios

## Servicios Activos

### Aplicación Principal
- **URL**: http://localhost:8080
- **Estado**: ✅ ACTIVO
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus

### Base de Datos PostgreSQL
- **Host**: localhost:5432
- **Usuario**: iso20022_user
- **Base de Datos**: iso20022_db
- **Estado**: ✅ ACTIVO

### Redis (Cache)
- **Host**: localhost:6379
- **Estado**: ✅ ACTIVO

### Prometheus (Monitoring)
- **URL**: http://localhost:9090
- **Estado**: ✅ ACTIVO
- **Configuración**: Recolectando métricas de la aplicación cada 5 segundos

### Grafana (Dashboards)
- **URL**: http://localhost:3000
- **Usuario**: admin / admin
- **Estado**: ✅ ACTIVO

### PgAdmin (Administración BD)
- **URL**: http://localhost:5050
- **Usuario**: admin@admin.com / admin
- **Estado**: ✅ ACTIVO

## Comandos Útiles

### Ver estado de todos los servicios
```bash
docker-compose ps
```

### Ver logs de la aplicación
```bash
docker logs iso20022-simulator --tail 50 -f
```

### Ver métricas de la aplicación
```bash
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
```

### Parar todos los servicios
```bash
docker-compose down
```

### Iniciar todos los servicios
```bash
docker-compose up -d
```

## Características Técnicas

- ✅ Aplicación Spring Boot 3.1.12 con Java 17
- ✅ Logging estructurado JSON con Logback
- ✅ Métricas con Micrometer/Prometheus
- ✅ Pool de conexiones HikariCP
- ✅ Contenedores Docker optimizados
- ✅ Health checks configurados
- ✅ Usuario no-root en contenedores
- ✅ Multi-stage build para imágenes ligeras

## Próximos Pasos

1. Configurar dashboards en Grafana
2. Implementar endpoints de negocio ISO 20022
3. Añadir pruebas de integración
4. Configurar CI/CD pipeline
