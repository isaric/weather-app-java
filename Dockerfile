# Multi-stage build for Weather App Java

# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy gradle files first for better layer caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies (will be cached if no changes)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Environment variables
ENV AI_API_KEY=""
ENV GOOGLE_GEMINI_MODEL="gemini-1.5-flash"

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]