# Stage 1: Build the fat JAR
FROM sbtscala/scala-sbt:eclipse-temurin-alpine-22_36_1.10.2_2.13.14 AS builder
WORKDIR /app
COPY . .
RUN sbt assembly

# Stage 2: Run the app
FROM openjdk:11-ea-23-jre-slim
WORKDIR /app
COPY --from=builder /app/target/scala-2.13/mini_url-assembly-0.1.0-SNAPSHOT.jar mini_url-assembly-0.1.0-SNAPSHOT.jar
COPY --from=builder /app /app
EXPOSE 8080
CMD ["java", "-jar", "mini_url-assembly-0.1.0-SNAPSHOT.jar"]
