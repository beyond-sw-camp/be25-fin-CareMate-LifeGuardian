# 1. Python 3.11 경량 이미지 사용
FROM python:3.11-slim

# 2. 작업 디렉터리 설정
WORKDIR /app

# 3. 의존성 파일 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 4. 소스 코드 복사
COPY . .

# 5. uvicorn을 사용하여 FastAPI 애플리케이션 실행 (포트 8000 노출)
EXPOSE 8000
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
