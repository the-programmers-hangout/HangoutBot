#
# Build
#
FROM gradle:6.6.1-jdk14 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM openjdk:11.0.8-jre-slim
RUN mkdir /home/app
ENV HOME=/home/app
WORKDIR $HOME

COPY --from=build /home/gradle/src/build/libs/*.jar $HOME/hangoutbot.jar

ENTRYPOINT [ "java", "-jar", "hangoutbot.jar" ]
