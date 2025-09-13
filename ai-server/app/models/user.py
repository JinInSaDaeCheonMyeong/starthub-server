from pydantic import BaseModel
from typing import List, Optional
from enum import Enum
from datetime import datetime


class BusinessType(str, Enum):
    CONTENT_MEDIA = "CONTENT_MEDIA"
    FINTECH = "FINTECH"
    HEALTHCARE_BIO = "HEALTHCARE_BIO"
    EDUCATION_EDUTECH = "EDUCATION_EDUTECH"
    IT_SOFTWARE = "IT_SOFTWARE"
    ECOMMERCE = "ECOMMERCE"
    ETC = "ETC"


class UserProfileData(BaseModel):
    user_id: int
    username: Optional[str] = None
    introduction: Optional[str] = None


class UserInterestData(BaseModel):
    user_id: int
    business_type: BusinessType


class AnnouncementDto(BaseModel):
    id: int
    title: str
    organization: str
    support_field: Optional[str] = None
    target_age: Optional[str] = None
    region: Optional[str] = None
    content: str


class LikedAnnouncementDto(BaseModel):
    user_id: int
    announcement: AnnouncementDto
    liked_at: datetime


class BMCDto(BaseModel):
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


class UserInterestsPayload(BaseModel):
    """Spring Boot에서 받는 사용자별 관심사 데이터 전체"""
    user_profile: UserProfileData
    user_interests: List[UserInterestData]
    liked_announcements: List[LikedAnnouncementDto]
    bmc_data: List[BMCDto]


class UserEmbedding(BaseModel):
    """Pinecone에 저장될 사용자 임베딩 정보"""
    user_id: int
    embedding: List[float]
    metadata: dict
    updated_at: datetime