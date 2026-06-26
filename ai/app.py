import os
from typing import List
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import psycopg2
from openai import OpenAI
from dotenv import load_dotenv

# 로컬 환경 변수 로드 (.env 파일이 있으면 읽어옴)
load_dotenv()

app = FastAPI(title="AI Recommendation Lambda Mock Server")

# OpenAI 클라이언트 초기화
# 환경변수 OPENAI_API_KEY가 필요합니다.
openai_api_key = os.getenv("OPENAI_API_KEY")
if not openai_api_key:
    print("⚠️ WARNING: OPENAI_API_KEY environment variable is not set.")
client = OpenAI(api_key=openai_api_key)

# PostgreSQL 연결 정보 설정
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_NAME = os.getenv("DB_NAME", "postgres")
DB_USER = os.getenv("DB_USER", "postgres")
DB_PASSWORD = os.getenv("DB_PASSWORD", "password")
DB_PORT = os.getenv("DB_PORT", "5432")

def get_db_connection():
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            database=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD,
            port=DB_PORT
        )
        return conn
    except Exception as e:
        print(f"❌ Database connection failed: {e}")
        raise e

# Request / Response 스키마 정의
class RecommendRequest(BaseModel):
    queryText: str
    age: int

class RecommendResponse(BaseModel):
    recommendedRiders: List[str]

@app.get("/")
def health_check():
    return {"status": "healthy", "service": "AI Recommendation Mock"}

@app.post("/recommend", response_model=RecommendResponse)
def recommend_riders(req: RecommendRequest):
    if not openai_api_key:
        raise HTTPException(status_code=500, detail="OpenAI API key is not configured on the server.")

    conn = None
    try:
        # 1. OpenAI Embedding 생성 (1536차원)
        response = client.embeddings.create(
            input=req.queryText,
            model="text-embedding-3-small"
        )
        query_embedding = response.data[0].embedding

        # 2. PostgreSQL 연결 및 pgvector 유사도 쿼리 실행
        conn = get_db_connection()
        with conn.cursor() as cursor:
            # pgvector 임베딩 연산자 <=> (코사인 거리) 사용
            # 가입 가능 연령대 필터링 조건 포함
            query = """
                SELECT r.rider_name
                FROM insurance_recommend_pool r
                INNER JOIN insurance_coverage c ON r.rider_name = c.coverage_name
                WHERE c.min_target_age <= %s AND c.max_target_age >= %s
                ORDER BY r.embedding <=> %s::vector
                LIMIT 15;
            """
            # query_embedding을 float 리스트 형태로 바인딩
            cursor.execute(query, (req.age, req.age, query_embedding))
            rows = cursor.fetchall()
            
            # 중복 제거 및 리스트화
            recommended_riders = []
            for row in rows:
                rider_name = row[0]
                if rider_name not in recommended_riders:
                    recommended_riders.append(rider_name)

        return RecommendResponse(recommendedRiders=recommended_riders)

    except Exception as e:
        print(f"❌ Error during recommendation: {e}")
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        if conn:
            conn.close()

if __name__ == "__main__":
    import uvicorn
    # 로컬에서 실행할 때: python app.py
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
