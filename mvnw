#!/bin/bash

# Maven wrapper - permite ejecutar Maven sin instalación local
if [ ! -f "mvnw" ]; then
    echo "Generando Maven wrapper..."
    mvn wrapper:wrapper -Dmaven=3.9.6
fi

# Hacer ejecutable el wrapper
chmod +x mvnw

echo "Maven wrapper configurado correctamente"
echo "Uso: ./mvnw clean install"
