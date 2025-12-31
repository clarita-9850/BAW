#!/bin/bash

# Rebuild and Restart Script with Cache Cleaning
# This script ensures fresh builds and no cache issues

echo "🧹 Cleaning previous build files and cache..."

# Clean Maven build files
echo "📦 Cleaning Maven target directory..."
rm -rf target/

# Clean Docker cache and images
echo "🐳 Cleaning Docker cache and images..."
docker-compose down
docker system prune -f
docker image prune -f

# Clean any existing containers
echo "🗑️ Removing existing containers..."
docker container prune -f

# Build fresh Maven project
echo "🔨 Building Maven project..."
mvn clean package -DskipTests

# Check if build was successful
if [ $? -ne 0 ]; then
    echo "❌ Maven build failed!"
    exit 1
fi

# Verify JAR file exists and has content
echo "✅ Verifying JAR file..."
if [ ! -f "target/kafka-event-driven-app-0.0.1-SNAPSHOT.jar" ]; then
    echo "❌ JAR file not found!"
    exit 1
fi

JAR_SIZE=$(stat -c%s "target/kafka-event-driven-app-0.0.1-SNAPSHOT.jar" 2>/dev/null || stat -f%z "target/kafka-event-driven-app-0.0.1-SNAPSHOT.jar" 2>/dev/null)
echo "📊 JAR file size: $JAR_SIZE bytes"

if [ $JAR_SIZE -lt 1000000 ]; then
    echo "⚠️ Warning: JAR file seems too small ($JAR_SIZE bytes)"
fi

# Build and start with no cache
echo "🚀 Building and starting Docker containers with no cache..."
docker-compose build --no-cache
docker-compose up -d

# Wait for services to start
echo "⏳ Waiting for services to start..."
sleep 15

# Check if services are running
echo "🔍 Checking service status..."
docker-compose ps

# Test if the application is responding
echo "🧪 Testing application health..."
sleep 5
curl -s http://localhost:8080/api/pipeline/status > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Application is responding!"
else
    echo "⚠️ Application may not be ready yet. Check logs with: docker-compose logs spring-app"
fi

echo "🎉 Rebuild and restart completed!"
echo "📱 Frontend URL: http://localhost:8080/react-dashboard.html"
echo "📊 API Status: http://localhost:8080/api/pipeline/status"