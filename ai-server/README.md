# StartHub AI Recommendation Service

StartHub의 사용자 맞춤형 창업 지원 공고 추천 시스템

## 📋 개요

이 서비스는 사용자의 관심사, 좋아요한 공고, BMC(Business Model Canvas) 데이터를 분석하여 개인화된 창업 지원 공고를 추천합니다.

## 🏗️ 아키텍처

- **FastAPI**: 웹 프레임워크
- **OpenAI Embeddings**: 텍스트 임베딩 생성
- **Pinecone**: 벡터 데이터베이스
- **Spring Boot**: 메인 서버와 데이터 연동

## 🚀 설치 및 실행

### 1. 의존성 설치

```bash
cd ai-server
pip install -r requirements.txt
```

### 2. 환경변수 설정

`.env.example`을 복사하여 `.env` 파일 생성:

```bash
cp .env.example .env
```

필수 환경변수:
```env
PINECONE_API_KEY=your_pinecone_api_key_here
OPENAI_API_KEY=your_openai_api_key_here
```

### 3. 서버 실행

```bash
# 개발 서버 실행
python run.py

# 또는 직접 실행
python -m uvicorn app.main:app --host localhost --port 8001 --reload
```

서버가 실행되면 다음 주소에서 접근 가능합니다:
- API 서버: http://localhost:8001
- Swagger 문서: http://localhost:8001/docs

## 📡 API 엔드포인트

### 사용자 관련
- `POST /api/v1/users/interests` - 사용자 관심사 데이터 수신
- `GET /api/v1/users/{user_id}/recommendations` - 사용자별 맞춤 추천
- `POST /api/v1/users/{user_id}/interactions` - 사용자 상호작용 기록
- `DELETE /api/v1/users/{user_id}/data` - 사용자 데이터 삭제

### 공고 관련
- `POST /api/v1/announcements/batch` - 공고 배치 처리
- `POST /api/v1/announcements/single` - 단일 공고 처리
- `GET /api/v1/announcements/search` - 키워드 기반 공고 검색
- `DELETE /api/v1/announcements/{announcement_id}` - 공고 삭제

### 시스템
- `GET /` - 서비스 상태
- `GET /health` - 헬스체크
- `GET /api/v1/stats` - 서비스 통계

## 🔄 Spring Boot와의 연동

Spring Boot 서버에서 다음과 같이 데이터를 전송합니다:

1. **사용자 관심사 동기화** (매일 06:00)
   ```
   Spring Boot → POST /api/v1/users/interests
   ```

2. **신규 공고 전송** (스크래핑 후)
   ```
   Spring Boot → POST /api/v1/announcements/batch
   ```

## 🧠 추천 알고리즘

### 사용자 임베딩 생성
1. **자기소개** (30% 가중치)
2. **관심 분야** (20% 가중치)
3. **좋아요한 공고들** (40% 가중치)
4. **BMC 데이터** (10% 가중치)

### 추천 과정
1. 사용자 임베딩과 공고 임베딩 간 코사인 유사도 계산
2. ACTIVE 상태 공고만 필터링
3. 지역, 지원분야 등 추가 필터 적용
4. 다양성을 위한 후처리 (같은 기관 중복 제거)

## 📊 데이터 플로우

```
[사용자 행동] → [Spring Boot DB] → [스케줄러] → [AI 서버] → [Pinecone]
                                                     ↓
[사용자 요청] ← [Spring Boot API] ← [추천 결과] ← [AI 서버]
```

## 🔍 모니터링

- **헬스체크**: `/health`
- **서비스 통계**: `/api/v1/stats`
- **로그**: 콘솔에 실시간 출력

## 🛠️ 개발 가이드

### 프로젝트 구조
```
ai-server/
├── app/
│   ├── main.py              # FastAPI 앱
│   ├── models/              # Pydantic 모델
│   ├── services/            # 비즈니스 로직
│   ├── routers/             # API 라우터
│   └── core/                # 설정 및 의존성
├── requirements.txt         # Python 의존성
├── .env.example            # 환경변수 템플릿
└── run.py                  # 서버 실행 스크립트
```

### 새로운 기능 추가
1. `models/`에 Pydantic 모델 정의
2. `services/`에 비즈니스 로직 구현
3. `routers/`에 API 엔드포인트 추가
4. `main.py`에 라우터 등록

## 🚨 문제 해결

### 일반적인 문제들

1. **Pinecone 연결 실패**
   - API 키 확인
   - 환경(region) 설정 확인

2. **OpenAI API 오류**
   - API 키 유효성 확인
   - 사용량 한도 확인

3. **임베딩 생성 실패**
   - 텍스트 길이 제한 확인
   - 특수 문자 처리 확인

### 로그 확인
```bash
# 상세 로그 출력
LOG_LEVEL=DEBUG python run.py
```

## 📝 TODO

- [ ] 실시간 학습 기능 구현
- [ ] 협업 필터링 추가
- [ ] A/B 테스트 지원
- [ ] 성능 최적화 (캐싱, 배치 처리)
- [ ] 모니터링 도구 연동