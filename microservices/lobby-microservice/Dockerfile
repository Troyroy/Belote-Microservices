# Build stage
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /app

# Copy gradle configuration files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

# Copy shared modules FIRST (required dependencies)
COPY shared ./shared

# Copy the specific microservice
COPY microservices/lobby-microservice ./microservices/lobby-microservice

# Build the application
RUN gradle :microservices:lobby-microservice:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /opt/app

# Copy jar from builder stage
COPY --from=builder /app/microservices/lobby-microservice/build/libs/lobby-microservice.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]