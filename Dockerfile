# Build stage
FROM gradle:8.10-jdk21 AS build

WORKDIR /app

# Copy gradle wrapper and cache dependencies
COPY gradlew gradlew.bat ./
COPY gradle/ ./gradle/
COPY build.gradle settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (this will be cached if dependencies don't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew clean build -x test --no-daemon

# Rename the JAR to a fixed name
RUN mv build/libs/*.jar build/libs/app.jar

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install wget for health check
RUN apk add --no-cache wget

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/app.jar ./

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose the application port
EXPOSE 8080

# Health check for Cloud Run
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
