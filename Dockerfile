# 1. Java 17
FROM eclipse-temurin:17-jdk-alpine

# 2. 작업 디렉토리
WORKDIR /app

# 3. 전체 프로젝트 복사
COPY . .

# 4. gradlew 권한 부여
RUN chmod +x ./gradlew

# 5. 빌드 (테스트 제외)
RUN ./gradlew clean build -x test

# 6. Spring Boot 실행 (bootJar만 실행)
ENTRYPOINT ["java", "-jar", "build/libs/Hanseoton-0.0.1-SNAPSHOT.jar"]
