FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY signal-payment-service/pom.xml signal-payment-service/
COPY signal-payment-service/src signal-payment-service/src

WORKDIR /workspace/signal-payment-service
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/signal-payment-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
