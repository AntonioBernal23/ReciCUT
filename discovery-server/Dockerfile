FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

RUN apk add --no-cache maven

# Copiar SOLO pom principal y del módulo
COPY pom.xml .

COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY user-service/pom.xml user-service/pom.xml

# Descargar dependencias y cachearlas
RUN mvn dependency:go-offline -pl discovery-server -am

# Ahora copiar el resto del código
COPY . .

# Compilar
RUN mvn clean package -DskipTests -pl discovery-server -am

# Runtime
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl bash && addgroup -S app && adduser -S -G app app

WORKDIR /app

COPY --from=build --chown=app:app /app/discovery-server/target/*.jar app.jar

USER app:app

EXPOSE 8050

ENTRYPOINT ["java", "-jar", "app.jar"]