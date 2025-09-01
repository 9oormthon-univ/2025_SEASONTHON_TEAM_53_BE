FROM openjdk:21-jdk-bullseye AS build

WORKDIR /app

  # Gradle Wrapper와 설정파일 복사
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

  # 소스 복사
COPY src ./src

  # 권한 부여 (윈도우에서 gradlew 복사 시 권한 문제 방지용)
RUN chmod +x ./gradlew

  # 테스트 제외하고 빌드 (테스트 포함하려면 -x test 빼기)
RUN ./gradlew clean bootJar -x test

  # 2) Run stage: 가벼운 JRE Alpine 이미지
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

  # 빌드된 jar 복사
COPY --from=build /app/build/libs/*.jar app.jar

  # 환경변수 .env 파일을 통해 받아서 JVM 옵션에 넣고 싶으면 여기서 처리 가능 (예: -Dspring.profiles.active=prod 등)

ENTRYPOINT ["java", "-jar", "app.jar"]