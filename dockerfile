#
# Build
#
FROM gradle:4.7.0-jdk8-alpine AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

#
# Run
#
FROM openjdk:8-jre-slim

RUN mkdir /home/app
ENV HOME=/home/app
WORKDIR $HOME

COPY config $HOME/config
COPY --from=build /home/gradle/src/build/libs/*.jar $HOME/spring-boot-application.jar

ENTRYPOINT [ "java", "-jar", "spring-boot-application.jar" ]