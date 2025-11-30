FROM eclipse-temurin:17-jdk
MAINTAINER Traversium Developers
WORKDIR /opt/audit-service

COPY audit-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/audit-service/app.jar"]

