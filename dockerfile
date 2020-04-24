#
# Build stage
#
FROM maven:3-jdk-8-slim AS build

ENV HOME=/home/app
RUN mkdir -p $HOME
WORKDIR $HOME

ADD pom.xml $HOME
RUN mvn verify clean --fail-never

ADD . $HOME
RUN mvn package

FROM debian:buster-slim as wait-for-it
RUN apt-get update && apt-get install -y wait-for-it

#
# Package stage
#
FROM openjdk:8-alpine

ENV HOME=/home/app
WORKDIR $HOME

RUN apk add --no-cache bash

COPY config $HOME/config
COPY --from=wait-for-it /usr/bin/wait-for-it /usr/bin/wait-for-it
COPY --from=build $HOME/target/hangoutbot*-jar-with-dependencies.jar $HOME/hangoutbot.jar

ENTRYPOINT [ "/bin/bash", "-c" ]