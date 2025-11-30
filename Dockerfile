FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY src/main/java .
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test
ENTRYPOINT ["java", "-jar", "build/libs/Hanseoton-0.0.1-SNAPSHOT.jar"]