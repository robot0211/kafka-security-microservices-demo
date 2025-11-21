# Multi-stage Dockerfile for Spring Boot service
FROM eclipse-temurin:17-jdk as builder
WORKDIR /app
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]


