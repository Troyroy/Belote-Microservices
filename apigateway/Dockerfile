# Build stage
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /app

# Copy gradle configuration files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

# Copy the specific microservice
COPY apigateway ./apigateway

# Build the application
RUN gradle :apigateway:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /opt/app

# Copy jar from builder stage
COPY --from=builder /app/apigateway/build/libs/apigateway-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]