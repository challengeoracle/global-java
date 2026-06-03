FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY signal-sales-service/pom.xml signal-sales-service/
COPY signal-sales-service/src signal-sales-service/src

WORKDIR /workspace/signal-sales-service
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/signal-sales-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
