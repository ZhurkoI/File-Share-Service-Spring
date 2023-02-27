FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY gradle/ ./gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src/ ./src/
RUN ./gradlew bootJar
COPY build/libs/*.jar app.jar

ENV DB_HOST=""
ENV DB_PORT=""
ENV DB_NAME=""
ENV DB_USERNAME=""
ENV DB_PASSWORD=""
ENV AWS_ACCESS_KEY_ID=""
ENV AWS_SECRET_ACCESS_KEY=""
ENTRYPOINT ["java", "-Dspring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?serverTimezone=UTC", "-Dspring.datasource.username=${DB_USERNAME}", "-Dspring.datasource.password=${DB_PASSWORD}", "-Dspring.liquibase.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?serverTimezone=UTC", "-Dspring.liquibase.user=${DB_USERNAME}", "-Dspring.liquibase.password=${DB_PASSWORD}", "-Daws.accessKeyId=${AWS_ACCESS_KEY_ID}", "-Daws.secretAccessKey=${AWS_SECRET_ACCESS_KEY}", "-jar","app.jar"]
