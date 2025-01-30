# Use an official Maven image to build the project
FROM maven:3.8.6-openjdk-11 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and source code to the container
COPY pom.xml .
COPY src ./src

# Run Maven to build the project and create the JAR file
RUN mvn clean package -DskipTests

# Use a minimal OpenJDK image for the runtime
FROM openjdk:11-jre-slim

# Set the working directory in the runtime container
WORKDIR /app

# Copy the built JAR file from the build container
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot application will run on
EXPOSE 8080

# Command to run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
