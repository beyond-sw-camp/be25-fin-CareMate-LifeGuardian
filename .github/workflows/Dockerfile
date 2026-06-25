# 1. 실행 환경 구성 (경량화된 OpenJDK 21 알프스 또는 슬림 이미지 사용)
FROM openjdk:21-jdk-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. Maven 빌드로 생성된 jar 파일을 컨테이너 내부로 복사
COPY target/lifeguardian-0.0.1-SNAPSHOT.jar app.jar

# 4. 컨테이너가 열어줄 스프링 부트 기본 포트 선언
EXPOSE 8080

# 5. 컨테이너 실행 시 스프링 부트 가동 환경 선언
# EC2 인스턴스의 타임존을 맞추고, 외부 .env 파일이나 인프라 환경변수를 주입받도록 세팅
ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "-Xms256m", "-Xmx512m", "app.jar"]
