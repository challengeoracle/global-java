FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY signal-analytics-ai-service/pom.xml signal-analytics-ai-service/
COPY signal-analytics-ai-service/src signal-analytics-ai-service/src

WORKDIR /workspace/signal-analytics-ai-service
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/signal-analytics-ai-service/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
