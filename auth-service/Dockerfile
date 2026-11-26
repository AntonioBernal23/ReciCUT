# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

RUN apk add --no-cache maven

# Copiar SOLO pom principal y del módulo
COPY pom.xml .

COPY campania-service/pom.xml campania-service/pom.xml
COPY recompensas/pom.xml recompensas/pom.xml

COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY collection-service/pom.xml collection-service/pom.xml
COPY report-service/pom.xml report-service/pom.xml
COPY recolectores/pom.xml recolectores/pom.xml
COPY centro_reciclajes/pom.xml centro_reciclajes/pom.xml

# Descargar dependencias y cachearlas
# El cache de Maven vive FUERA de la imagen, en un cache persistente de
# BuildKit. Importa porque cada Dockerfile copia los 11 pom.xml: tocar el
# pom de CUALQUIER modulo invalida esta capa en TODOS los servicios y, sin
# el cache, Maven volveria a descargar medio Maven Central cada vez.
#
# sharing=locked serializa el acceso: si se construyen varios servicios a
# la vez, no compiten por la red ni se pisan el repositorio local.
RUN --mount=type=cache,target=/root/.m2,sharing=locked mvn dependency:go-offline -pl auth-service -am

# Ahora copiar el resto del código
COPY . .

# Compilar
RUN --mount=type=cache,target=/root/.m2,sharing=locked mvn clean package -DskipTests -pl auth-service -am

# Runtime
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl bash && addgroup -S app && adduser -S -G app app

WORKDIR /app

COPY --from=build --chown=app:app /app/auth-service/target/*.jar app.jar

USER app:app

EXPOSE 8070

ENTRYPOINT ["java", "-jar", "app.jar"]
