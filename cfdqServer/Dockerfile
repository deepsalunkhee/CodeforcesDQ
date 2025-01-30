# Use an official JDK runtime as a parent image
FROM openjdk:21-jdk

# Set the working directory
WORKDIR /app

# Copy the JAR file from the target directory
COPY target/*.jar app.jar

# Expose the port (if using Spring Boot, default is 8080)
EXPOSE 8080

# Run the JAR file
CMD ["java", "-jar", "app.jar"]
