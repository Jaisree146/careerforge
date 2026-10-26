# -------------------------------------------------------------
# Stage 1: Build Stage with Maven & Eclipse Temurin JDK 21
# -------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Cache dependency layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code and package executable fat jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# -------------------------------------------------------------
# Stage 2: Hardened, Minimal Production Runtime Container
# -------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Install curl for container health check probes
RUN apk add --no-cache curl

# Security: Create non-root system user and group
RUN addgroup -S careerforge && adduser -S careerforge -G careerforge

# Create storage upload directory for PDF resumes
RUN mkdir -p /app/uploads/resumes && chown -R careerforge:careerforge /app

USER careerforge

# Copy packaged jar artifact from builder stage
COPY --from=builder --chown=careerforge:careerforge /app/target/*.jar app.jar

ENV SERVER_PORT=8080 \
    FILE_UPLOAD_DIR=/app/uploads/resumes \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
