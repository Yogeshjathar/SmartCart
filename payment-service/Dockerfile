FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the built jar file into the container
COPY target/*-1.0.0.jar app.jar

EXPOSE 8086

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
