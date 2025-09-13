"""
FastAPI 서버용 사용자 관심사 데이터 전송 API

이 파일은 Spring Boot 서버에서 사용자별 관심사 데이터를 FastAPI 서버로 전송하는 API 예시입니다.
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional, Dict
from enum import Enum
import httpx
from datetime import datetime

app = FastAPI()

# 비즈니스 타입 enum (Spring Boot의 BusinessType과 동일)
class BusinessType(str, Enum):
    CONTENT_MEDIA = "CONTENT_MEDIA"
    FINTECH = "FINTECH"
    HEALTHCARE_BIO = "HEALTHCARE_BIO"
    EDUCATION_EDUTECH = "EDUCATION_EDUTECH"
    IT_SOFTWARE = "IT_SOFTWARE"
    ECOMMERCE = "ECOMMERCE"
    ETC = "ETC"

# Pydantic 모델들
class UserInterestData(BaseModel):
    user_id: int
    business_type: BusinessType

class AnnouncementData(BaseModel):
    id: int
    title: str
    organization: str
    support_field: Optional[str] = None
    target_age: Optional[str] = None
    region: Optional[str] = None
    content: str

class LikedAnnouncementData(BaseModel):
    user_id: int
    announcement: AnnouncementData
    liked_at: datetime

class BMCData(BaseModel):
    id: int
    user_id: int
    title: str
    key_partners: Optional[str] = None
    key_activities: Optional[str] = None
    key_resources: Optional[str] = None
    value_proposition: Optional[str] = None
    customer_relationships: Optional[str] = None
    channels: Optional[str] = None
    customer_segments: Optional[str] = None
    cost_structure: Optional[str] = None
    revenue_streams: Optional[str] = None
    is_completed: bool

class UserProfileData(BaseModel):
    user_id: int
    username: Optional[str] = None
    introduction: Optional[str] = None

class UserInterestsPayload(BaseModel):
    """사용자별 관심사 데이터를 모두 포함한 페이로드"""
    user_profile: UserProfileData
    user_interests: List[UserInterestData]
    liked_announcements: List[LikedAnnouncementData]
    bmc_data: List[BMCData]

class AnnouncementBatch(BaseModel):
    """공고 배치 데이터"""
    announcements: List[AnnouncementData]
    batch_id: str
    timestamp: datetime

# 사용자별 관심사 데이터 수신 API
@app.post("/api/v1/users/interests")
async def receive_user_interests(payload: UserInterestsPayload):
    """
    Spring Boot 서버에서 사용자별 관심사 데이터를 받는 API
    
    - user_profile: 사용자 프로필 정보 (자기소개 포함)
    - user_interests: 회원가입 시 선택한 관심 분야
    - liked_announcements: 좋아요 표시한 공고들
    - bmc_data: 작성한 BMC 데이터
    """
    try:
        user_id = payload.user_profile.user_id
        
        # 여기서 Pinecone에 저장하기 위한 벡터 생성 및 저장 로직 구현
        # 1. 사용자 프로필의 자기소개 벡터화
        # 2. 관심 분야 정보 벡터화
        # 3. 좋아요한 공고들의 내용 벡터화
        # 4. BMC 데이터 벡터화
        # 5. 사용자별로 Pinecone에 저장
        
        print(f"사용자 {user_id}의 관심사 데이터 수신:")
        print(f"- 프로필: {payload.user_profile}")
        print(f"- 관심분야: {len(payload.user_interests)}개")
        print(f"- 좋아요 공고: {len(payload.liked_announcements)}개")
        print(f"- BMC 데이터: {len(payload.bmc_data)}개")
        
        # TODO: 실제 Pinecone 저장 로직 구현
        await store_user_interests_to_pinecone(payload)
        
        return {
            "success": True,
            "message": f"사용자 {user_id}의 관심사 데이터가 성공적으로 처리되었습니다.",
            "user_id": user_id
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"데이터 처리 중 오류 발생: {str(e)}")

# 공고 배치 수신 API
@app.post("/api/v1/announcements/batch")
async def receive_announcements_batch(batch: AnnouncementBatch):
    """
    크롤링된 공고들을 배치로 받는 API
    """
    try:
        print(f"공고 배치 수신: {batch.batch_id}, {len(batch.announcements)}개 공고")
        
        # 공고들을 Pinecone에 벡터화하여 저장
        # TODO: 실제 Pinecone 저장 로직 구현
        await store_announcements_to_pinecone(batch.announcements, batch.batch_id)
        
        return {
            "success": True,
            "message": f"배치 {batch.batch_id}의 {len(batch.announcements)}개 공고가 성공적으로 처리되었습니다.",
            "batch_id": batch.batch_id,
            "processed_count": len(batch.announcements)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"공고 배치 처리 중 오류 발생: {str(e)}")

# 사용자별 맞춤 공고 추천 API
@app.get("/api/v1/users/{user_id}/recommendations")
async def get_user_recommendations(user_id: int, limit: int = 10):
    """
    사용자별 관심사에 맞는 공고 추천
    """
    try:
        # TODO: Pinecone에서 사용자 관심사와 유사한 공고 검색
        recommendations = await get_recommendations_from_pinecone(user_id, limit)
        
        return {
            "user_id": user_id,
            "recommendations": recommendations,
            "count": len(recommendations)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"추천 생성 중 오류 발생: {str(e)}")

# Helper 함수들 (실제 구현 필요)
async def store_user_interests_to_pinecone(payload: UserInterestsPayload):
    """사용자 관심사 데이터를 Pinecone에 저장"""
    # 실제 Pinecone 클라이언트와 임베딩 모델을 사용하여 구현
    pass

async def store_announcements_to_pinecone(announcements: List[AnnouncementData], batch_id: str):
    """공고 데이터를 Pinecone에 저장"""
    # 실제 Pinecone 클라이언트와 임베딩 모델을 사용하여 구현
    pass

async def get_recommendations_from_pinecone(user_id: int, limit: int):
    """Pinecone에서 사용자별 맞춤 공고 검색"""
    # 실제 Pinecone 검색 로직 구현
    return []

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)