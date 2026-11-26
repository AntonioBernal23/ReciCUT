# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY centro_reciclajes/pom.xml centro_reciclajes/pom.xml
COPY recolectores/pom.xml recolectores/pom.xml
COPY report-service/pom.xml report-service/pom.xml
COPY collection-service/pom.xml collection-service/pom.xml

COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY campania-service/pom.xml campania-service/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY recompensas/pom.xml recompensas/pom.xml
COPY user-service/pom.xml user-service/pom.xml

# El cache de Maven vive FUERA de la imagen, en un cache persistente de
# BuildKit. Importa porque cada Dockerfile copia los 11 pom.xml: tocar el
# pom de CUALQUIER modulo invalida esta capa en TODOS los servicios y, sin
# el cache, Maven volveria a descargar medio Maven Central cada vez.
#
# sharing=locked serializa el acceso: si se construyen varios servicios a
# la vez, no compiten por la red ni se pisan el repositorio local.
RUN --mount=type=cache,target=/root/.m2,sharing=locked mvn -B dependency:go-offline -pl campania-service -am

COPY . .

RUN --mount=type=cache,target=/root/.m2,sharing=locked mvn -B clean package -DskipTests -pl campania-service -am

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S -G app app

COPY --from=build --chown=app:app /app/campania-service/target/*.jar app.jar

USER app

EXPOSE 8061

ENTRYPOINT ["java", "-jar", "app.jar"]
