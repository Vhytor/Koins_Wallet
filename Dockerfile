# Multi-stage Dockerfile: build with Maven then run the produced jar
FROM maven:3.8.8-eclipse-temurin-17 as builder
WORKDIR /workspace
# Copy source
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
# Copy the project
COPY src ./src
# Ensure permissions on wrapper
RUN chmod +x mvnw || true
# Build
RUN ./mvnw -B -DskipTests package

# Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy the jar produced by the builder stage
COPY --from=builder /workspace/target/WalletSystem-0.0.1-SNAPSHOT.jar app.jar
# Expose port
EXPOSE 8080
# Default envs (can be overridden by docker-compose or env vars)
ENV SPRING_PROFILES_ACTIVE=default
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/koins_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=root
ENV APP_JWT_SECRET=verysecretkeychangeme

ENTRYPOINT ["java","-jar","/app/app.jar"]

