## Multi-stage build to avoid relying on mvnw/.mvn in context

# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom and download dependencies first for better caching
COPY pom.xml .
RUN mvn -B -e -U -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -B -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/cmips-mvp-1.0.0.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]


