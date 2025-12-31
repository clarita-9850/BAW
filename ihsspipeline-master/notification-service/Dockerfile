FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the built jar
COPY target/notification-service-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8081

# Run the application
CMD ["java", "-jar", "app.jar"]
