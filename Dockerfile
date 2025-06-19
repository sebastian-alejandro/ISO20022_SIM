# Dockerfile multi-stage para optimizar tamaño de imagen
FROM eclipse-temurin:17-jdk-alpine AS build

# Instalar Maven
RUN apk add --no-cache maven

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración Maven
COPY pom.xml .

# Descargar dependencias (para aprovechar cache de Docker)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar aplicación
RUN mvn clean package -DskipTests

# Segunda etapa: imagen de runtime
FROM eclipse-temurin:17-jre-alpine AS runtime

# Crear usuario no-root para seguridad
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

# Crear directorios necesarios
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# Cambiar a usuario no-root
USER appuser

WORKDIR /app

# Copiar JAR desde stage de build
COPY --from=build --chown=appuser:appgroup /app/target/iso20022-simulator-*.jar app.jar

# Exponer puerto de la aplicación
EXPOSE 8080

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200" \
    SPRING_PROFILES_ACTIVE=prod

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
