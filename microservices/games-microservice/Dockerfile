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
COPY microservices/games-microservice ./microservices/games-microservice

# Build the application
RUN gradle :microservices:games-microservice:bootJar --no-daemon

# Runtime stage
FROM openjdk:17-jdk-alpine

WORKDIR /opt/app

# Copy jar from builder stage
COPY --from=builder /app/microservices/games-microservice/build/libs/games-microservice.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]