FROM gradle:jdk11 as builder
USER root
COPY . /home/gradle
WORKDIR /home/gradle
RUN gradle build -x test

FROM openjdk:11
WORKDIR /auth-server/
COPY --from=bulder /home/gradle/build/libs/*.jar /auth-server/app.jar
ENTRYPOINT ["java", "-Xms128m -Xmx256m -XX:+UseZGC", "app.jar"]
