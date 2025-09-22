# ----------------------------
# Stage 1: Build the JAR
# ----------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set working dir
WORKDIR /app

# Copy pom.xml and download dependencies first (caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy all sources
COPY src ./src

# Build jar (skip tests for faster Docker builds)
RUN mvn clean package -DskipTests

# ----------------------------
# Stage 2: Run the app
# ----------------------------
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the jar from the builder stage
COPY --from=builder /app/target/app.jar app.jar

# Run Spring Boot CLI
ENTRYPOINT ["java", "-jar", "app.jar"]
