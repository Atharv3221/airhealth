# One image for Railway: the React build is bundled into the Spring Boot jar,
# so a single URL serves both the UI and the API.

# 1. Build the React app
FROM node:22-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# 2. Build the Spring Boot jar with the React build as static resources
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn -q dependency:go-offline
COPY backend/src ./src
RUN rm -rf src/main/resources/static
COPY --from=frontend /app/frontend/dist ./src/main/resources/static
# tests run locally; the build container has no database or secrets
RUN mvn -q clean package -DskipTests

# 3. Run it (Railway provides PORT; application.properties reads it)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/backend/target/airhealth-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
