#
# Build stage
#
FROM maven:3-jdk-8-slim AS build

ENV HOME=/home/app
RUN mkdir -p $HOME
WORKDIR $HOME

ADD pom.xml $HOME
RUN mvn verify clean --fail-never

ADD ./src $HOME/src
RUN mvn package

#
# Package stage
#
FROM openjdk:8-alpine

ENV HOME=/home/app
WORKDIR $HOME

COPY config $HOME/config
COPY --from=build $HOME/target/hangoutbot*-jar-with-dependencies.jar $HOME/hangoutbot.jar

ENTRYPOINT [ "java", "-jar", "hangoutbot.jar" ]