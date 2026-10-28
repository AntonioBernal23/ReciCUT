# =====================================================================
#  report-service
#
#  Build multi-etapa: la etapa "build" compila con JDK + Maven; la etapa
#  final solo lleva el JRE y el .jar. Asi la imagen que se despliega no
#  incluye Maven ni el codigo fuente: pesa menos y expone menos.
# =====================================================================

# ---------- Etapa 1: compilar ----------
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

RUN apk add --no-cache maven

# Copiar SOLO los pom primero. Si no cambian, Docker reutiliza la capa de
# dependencias cacheada y no vuelve a descargar medio Maven Central.
COPY pom.xml .

COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY collection-service/pom.xml collection-service/pom.xml
COPY report-service/pom.xml report-service/pom.xml

# Descargar dependencias y cachearlas
# -pl = solo este modulo    -am = y los que necesita (el pom padre)
RUN mvn dependency:go-offline -pl report-service -am

# Ahora si, el resto del codigo
COPY . .

RUN mvn clean package -DskipTests -pl report-service -am

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:21-jre-alpine

# curl lo usa el healthcheck del docker-compose
RUN apk add --no-cache curl bash && addgroup -S app && adduser -S -G app app

WORKDIR /app

COPY --from=build --chown=app:app /app/report-service/target/*.jar app.jar

# No correr como root dentro del contenedor (privilegio minimo)
USER app:app

EXPOSE 8040

ENTRYPOINT ["java", "-jar", "app.jar"]
