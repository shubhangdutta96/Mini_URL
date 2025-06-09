# Use OpenJDK 11 runtime image
FROM openjdk:11-jre-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the fat JAR file into the container
COPY target/scala-2.13/url_shortener_service.jar app.jar

# Expose the port your Akka HTTP app listens on
EXPOSE 8080

# Run the jar
CMD ["java", "-jar", "app.jar"]



// shubhang dutta
