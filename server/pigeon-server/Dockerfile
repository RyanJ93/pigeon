FROM gradle:7.0.2-jdk16 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:16.0.1-jdk
EXPOSE 2898
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/pigeon-server.jar
ENTRYPOINT ["java", "-jar", "/app/pigeon-server.jar", "--no-gui"]
