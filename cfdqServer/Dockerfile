FROM jelastic/maven:3.9.5-openjdk-21 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-slim
COPY --from=build /target/cfdqServer-0.0.1-SNAPSHOT.jar cfdqServer-0.0.1-SNAPSHOT.jar
CMD ["java", "-jar", "cfdqServer-0.0.1-SNAPSHOT.jar"]
