FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY gradle/ ./gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src/ ./src/
RUN ./gradlew bootJar
RUN mv build/libs/*.jar Application.jar

ENTRYPOINT ["java", "-jar","Application.jar"]
