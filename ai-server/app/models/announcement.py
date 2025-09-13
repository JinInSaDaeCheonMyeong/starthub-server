from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime
from enum import Enum


class AnnouncementStatus(str, Enum):
    ACTIVE = "ACTIVE"
    INACTIVE = "INACTIVE"


class AnnouncementData(BaseModel):
    id: int
    title: str
    organization: str
    support_field: Optional[str] = None
    target_age: Optional[str] = None
    region: Optional[str] = None
    content: str
    status: Optional[AnnouncementStatus] = AnnouncementStatus.ACTIVE


class AnnouncementBatch(BaseModel):
    """공고 배치 데이터"""
    announcements: List[AnnouncementData]
    batch_id: str
    timestamp: datetime


class AnnouncementEmbedding(BaseModel):
    """Pinecone에 저장될 공고 임베딩 정보"""
    announcement_id: int
    embedding: List[float]
    metadata: dict
    created_at: datetime


class RecommendationRequest(BaseModel):
    user_id: int
    limit: Optional[int] = 10
    page: Optional[int] = 1
    page_size: Optional[int] = 10
    filter_active_only: Optional[bool] = True
    filter_region: Optional[str] = None
    filter_support_field: Optional[str] = None


class RecommendationResponse(BaseModel):
    user_id: int
    recommendations: List[dict]
    total_count: int
    page: int
    page_size: int
    total_pages: int
    has_next: bool
    has_previous: bool
    generated_at: datetime