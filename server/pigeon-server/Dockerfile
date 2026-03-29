FROM gradle:9-jdk25 AS build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build --no-daemon

FROM azul/zulu-openjdk:25-latest

EXPOSE 2898

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/pigeon-server.jar
COPY ./docker/pigeon_change_password.sh /bin/pigeon_change_password
COPY ./docker/pigeon_useradd.sh /bin/pigeon_useradd

RUN chmod +x /bin/pigeon_change_password && \
    chmod +x /bin/pigeon_useradd

ENTRYPOINT ["java", "-jar", "/app/pigeon-server.jar", "--no-gui"]
