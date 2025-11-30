# 1. Java 17 버전을 기반으로 설정
FROM eclipse-temurin:17-jdk-alpine

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. [중요] 프로젝트의 '모든' 파일을 컨테이너로 복사 (점 두 개 사이 띄어쓰기 필수!)
COPY . .

# 4. gradlew 실행 권한 부여
RUN chmod +x ./gradlew

# 5. 빌드 실행 (테스트 생략)
RUN ./gradlew clean build -x test

# 6. 실행 명령어 (프로젝트 이름 Hanseoton 주의)
ENTRYPOINT ["java", "-jar", "build/libs/Hanseoton-0.0.1-SNAPSHOT.jar"]