# Stage 1: Build the application
FROM gradle:8.7.0-jdk21 AS build
WORKDIR /home/gradle/src
COPY ./app/build.gradle ./settings.gradle ./gradlew ./gradlew.bat ./
COPY gradle ./gradle
COPY ./app/src ./src
RUN gradle build --no-daemon

# Stage 2: Create the final image
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
ARG JAR_FILE=build/libs/ise--y2--b3--project--web-app-0.0.1-SNAPSHOT.jar
COPY --from=build /home/gradle/src/${JAR_FILE} /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
