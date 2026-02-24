FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the built jar file into the container
COPY target/*.jar app.jar

EXPOSE 8761

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]