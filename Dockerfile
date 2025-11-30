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

# 6. 실행할 jar 자동 탐색 후 실행
ENTRYPOINT ["sh", "-c", "java -jar $(ls build/libs/*.jar | head -n 1)"]
