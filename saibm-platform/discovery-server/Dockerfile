FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY libro-service/pom.xml libro-service/pom.xml
COPY inventario-service/pom.xml inventario-service/pom.xml
COPY membresia-service/pom.xml membresia-service/pom.xml
COPY discovery-server/src discovery-server/src
RUN mvn -B -pl discovery-server -am -DskipTests package

FROM eclipse-temurin:21.0.8_9-jre-alpine-3.22
RUN addgroup -S saibm && adduser -S -G saibm saibm
WORKDIR /app
COPY --from=build --chown=saibm:saibm /workspace/discovery-server/target/discovery-server-*.jar app.jar
USER saibm
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
