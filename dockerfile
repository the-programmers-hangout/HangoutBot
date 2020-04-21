#
# Build stage
#
FROM maven:3-jdk-8-slim AS build

COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:8-alpine

COPY config /home/app/config
COPY --from=build /home/app/target/tphbot*-jar-with-dependencies.jar /home/app/tphbot.jar
WORKDIR /home/app/
ENTRYPOINT ["java","-jar","tphbot.jar"]