FROM eclipse-temurin:17-jre
WORKDIR /app

# GitHub Actions에서 미리 빌드해 둔 jar를 COPY
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
