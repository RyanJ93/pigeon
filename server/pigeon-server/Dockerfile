FROM gradle:7.0.2-jdk16 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:16.0.1-jdk
EXPOSE 2898
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/pigeon-server.jar
RUN printf "#!/bin/bash\njava -jar /app/pigeon-server.jar --useradd \"\$@\"" > /bin/pigeon_useradd
RUN chmod +x /bin/pigeon_useradd
ENTRYPOINT ["java", "-jar", "/app/pigeon-server.jar", "--no-gui"]
