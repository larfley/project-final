FROM ubuntu:latest
LABEL authors="lara"

ENTRYPOINT ["top", "-b"]

# Используем официальный образ OpenJDK
FROM adoptopenjdk/openjdk16:alpine


WORKDIR /app


COPY build/libs/*.jar app.jar


CMD ["java", "-jar", "app.jar"]
