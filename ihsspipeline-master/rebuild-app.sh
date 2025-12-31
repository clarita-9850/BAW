#!/bin/bash

echo "🔄 Rebuilding application with fresh JAR and cache cleaning..."

# Clean Maven build files
echo "📦 Cleaning Maven target directory..."
rm -rf target/

# Clean Maven build
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

# Stop and remove existing containers
echo "🛑 Stopping existing containers..."
docker-compose down

# Clean Docker cache and images
echo "🐳 Cleaning Docker cache and images..."
docker system prune -f
docker image prune -f

# Remove the spring-app image to force rebuild
echo "🗑️ Removing old spring-app image..."
docker rmi trial-spring-app 2>/dev/null || true

# Set build arguments to force cache invalidation
export BUILD_DATE=$(date -u +%Y-%m-%dT%H:%M:%SZ)
export JAR_TIMESTAMP=$(date -u +%Y-%m-%dT%H:%M:%SZ)

# Rebuild and start with no cache
echo "🔨 Rebuilding Docker image with no cache and fresh timestamps..."
docker-compose build --no-cache spring-app

# Start the services
echo "🚀 Starting services..."
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

echo "🎉 Application rebuilt and started successfully!"
echo "📱 Frontend URL: http://localhost:8080/react-dashboard.html"
echo "📊 API Status: http://localhost:8080/api/pipeline/status"
