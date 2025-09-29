FROM openjdk:17-jdk-alpine AS builder

WORKDIR /opt/app

COPY build/libs/Belote-1.0-SNAPSHOT.jar /opt/app/Belote-1.0-SNAPSHOT.jar
COPY src/main/resources/application.properties application.properties


EXPOSE 8080

ENTRYPOINT ["java","-jar","Belote-1.0-SNAPSHOT.jar"]


