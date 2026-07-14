FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

COPY --chown=spring:spring --from=build /workspace/target/*.jar app.jar

USER spring:spring

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 CMD bash -ec "exec 3<>/dev/tcp/127.0.0.1/8080; printf 'GET /actuator/health/readiness HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n' >&3; grep -q '\"status\":\"UP\"' <&3"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
