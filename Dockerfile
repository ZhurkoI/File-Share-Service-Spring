FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY gradle/ ./gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src/ ./src/
RUN ./gradlew bootJar
RUN mv build/libs/*.jar Application.jar

ENTRYPOINT ["java", "-Dspring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?serverTimezone=UTC", "-Dspring.datasource.username=${DB_USERNAME}", "-Dspring.datasource.password=${DB_PASSWORD}", "-Dspring.liquibase.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?serverTimezone=UTC", "-Dspring.liquibase.user=${DB_USERNAME}", "-Dspring.liquibase.password=${DB_PASSWORD}", "-Daws.s3.bucket.name=${AWS_S3_BUCKET_NAME}","-Daws.s3.region=${AWS_S3_REGION}", "-jar","Application.jar"]
