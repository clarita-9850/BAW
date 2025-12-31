FROM eclipse-temurin:17-jdk

WORKDIR /app

# Add build arguments to force cache invalidation
ARG BUILD_DATE
ARG JAR_TIMESTAMP
RUN echo "Build date: $BUILD_DATE" > /app/build-info.txt
RUN echo "JAR timestamp: $JAR_TIMESTAMP" >> /app/build-info.txt

# Clean any existing files to ensure fresh build
RUN rm -rf /app/*

# Copy the built jar - this will be updated on every build
COPY target/timesheet-reporting-app-0.0.1-SNAPSHOT.jar app.jar

# Add timestamp to JAR to ensure it's always fresh
RUN touch -t $(date +%Y%m%d%H%M.%S) app.jar

# Verify the JAR file exists and has content
RUN ls -la /app/app.jar && echo "JAR file size: $(stat -c%s /app/app.jar) bytes"

# Expose port
EXPOSE 8080

# Run the application with increased heap size for large datasets
CMD ["java", "-Xmx2g", "-Xms512m", "-XX:+UseG1GC", "-jar", "app.jar"]
