# Multi-stage Dockerfile
# 1) Build stage: use Maven to build the jar using the project wrapper
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy Maven wrapper and pom first to leverage Docker layer caching
# Copy pom and sources and build using Maven installed in the build image
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package

# 2) Runtime stage: slim JRE image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from the build stage. The artifact produced by the build
# will be copied to app.jar. If your artifact name differs, the wildcard will
# still match the JAR in target/ produced by the build stage.

COPY --from=build /workspace/target/*.jar /app/app.jar

# Expose the port the application is configured to use (see application.yml)
EXPOSE 8081

# Provide sensible Java memory options and allow overriding via environment
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# (No healthcheck here to avoid requiring curl in the runtime image)

# Run the application. Using a shell form so JAVA_OPTS can be expanded.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
