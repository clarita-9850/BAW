FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the built jar
COPY target/external-validation-api-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8082

# Run the application
CMD ["java", "-jar", "app.jar"]
