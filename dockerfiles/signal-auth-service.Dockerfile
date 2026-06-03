FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY signal-auth-service/pom.xml signal-auth-service/
COPY signal-auth-service/src signal-auth-service/src

WORKDIR /workspace/signal-auth-service
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/signal-auth-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
