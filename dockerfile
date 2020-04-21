#
# Build stage
#
FROM maven:3-jdk-11-slim AS build

COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:11-alpine

COPY --from=build /home/app/target/tphbot*-jar-with-dependencies.jar /usr/local/lib/tphbot.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/tphbot.jar"]