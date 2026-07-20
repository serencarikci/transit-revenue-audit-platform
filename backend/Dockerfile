FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN apk add --no-cache curl \
	&& addgroup -S transit && adduser -S transit -G transit
COPY --from=build /workspace/target/*.jar app.jar
USER transit
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --retries=10 --start-period=60s \
	CMD curl -fsS http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
