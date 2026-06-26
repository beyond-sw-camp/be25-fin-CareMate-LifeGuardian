# 1. Java 21 실행 환경(JRE) 경량 이미지 사용
FROM eclipse-temurin:21-jre-alpine

# 2. 컨테이너 내부 작업 디렉터리 설정
WORKDIR /app

# 3. GitHub Actions 빌드 단계에서 생성된 JAR 파일을 복사
# (target 폴더 내의 jar 파일을 app.jar로 이름을 일원화하여 복사합니다)
COPY target/*.jar app.jar

# 4. Spring Boot 애플리케이션의 기본 포트인 8080 노출
EXPOSE 8080

# 5. 애플리케이션 실행을 위한 기본 엔트리포인트 지정
# 시간대 설정 등 필요한 JVM 옵션이 있다면 추가할 수 있습니다.
ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "app.jar"]
