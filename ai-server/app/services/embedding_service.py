import asyncio
import logging
from typing import List, Dict, Any
import numpy as np
from openai import AsyncOpenAI

from ..core.config import settings
from ..models.user import UserInterestsPayload, BusinessType
from ..models.announcement import AnnouncementData

logger = logging.getLogger(__name__)


class EmbeddingService:
    def __init__(self):
        self.client = AsyncOpenAI(api_key=settings.openai_api_key)
        self.model = settings.embedding_model
        
        # BusinessType을 한국어 설명으로 매핑
        self.business_type_mapping = {
            BusinessType.CONTENT_MEDIA: "콘텐츠 미디어 엔터테인먼트 방송 영상 제작",
            BusinessType.FINTECH: "핀테크 금융 결제 투자 블록체인 암호화폐",
            BusinessType.HEALTHCARE_BIO: "헬스케어 바이오 의료 건강 제약 의료기기",
            BusinessType.EDUCATION_EDUTECH: "교육 에듀테크 온라인 학습 스킬 개발",
            BusinessType.IT_SOFTWARE: "IT 소프트웨어 개발 프로그래밍 앱 시스템",
            BusinessType.ECOMMERCE: "전자상거래 온라인 쇼핑 이커머스 판매",
            BusinessType.ETC: "기타 다양한 분야 창업 사업"
        }
    
    async def get_embedding(self, text: str) -> List[float]:
        """텍스트를 임베딩으로 변환"""
        try:
            if not text or not text.strip():
                # 빈 텍스트의 경우 0 벡터 반환
                return [0.0] * settings.embedding_dimension
            
            response = await self.client.embeddings.create(
                input=text,
                model=self.model
            )
            return response.data[0].embedding
        except Exception as e:
            logger.error(f"임베딩 생성 실패: {text[:100]}... - {str(e)}")
            # 실패 시 0 벡터 반환
            return [0.0] * settings.embedding_dimension
    
    async def create_user_interest_embedding(self, payload: UserInterestsPayload) -> Dict[str, Any]:
        """사용자 관심사 데이터를 종합한 임베딩 생성"""
        try:
            # 1. 자기소개 임베딩
            introduction_text = payload.user_profile.introduction or ""
            introduction_embedding = await self.get_embedding(introduction_text)
            
            # 2. 관심 분야 임베딩
            interest_texts = []
            for interest in payload.user_interests:
                business_desc = self.business_type_mapping.get(interest.business_type, "")
                interest_texts.append(business_desc)
            
            interest_text = " ".join(interest_texts)
            interest_embedding = await self.get_embedding(interest_text)
            
            # 3. 좋아한 공고들의 임베딩
            liked_texts = []
            for liked in payload.liked_announcements:
                announcement = liked.announcement
                # 제목, 지원분야, 내용을 조합
                text_parts = [
                    announcement.title,
                    announcement.support_field or "",
                    announcement.content[:500] if announcement.content else ""  # 내용은 500자만
                ]
                liked_texts.append(" ".join(filter(None, text_parts)))
            
            liked_text = " ".join(liked_texts)
            liked_embedding = await self.get_embedding(liked_text)
            
            # 4. BMC 데이터 임베딩
            bmc_texts = []
            for bmc in payload.bmc_data:
                if bmc.is_completed:
                    bmc_parts = [
                        bmc.title,
                        bmc.value_proposition or "",
                        bmc.customer_segments or "",
                        bmc.key_activities or ""
                    ]
                    bmc_texts.append(" ".join(filter(None, bmc_parts)))
            
            bmc_text = " ".join(bmc_texts)
            bmc_embedding = await self.get_embedding(bmc_text)
            
            # 5. 가중평균으로 최종 임베딩 생성
            embeddings = []
            weights = []
            
            if any(val != 0 for val in introduction_embedding):
                embeddings.append(np.array(introduction_embedding))
                weights.append(0.1)  # 자기소개 10%
            
            if any(val != 0 for val in interest_embedding):
                embeddings.append(np.array(interest_embedding))
                weights.append(0.1)  # 관심분야 10%
            
            if any(val != 0 for val in liked_embedding):
                embeddings.append(np.array(liked_embedding))
                weights.append(0.4)  # 좋아한 공고 40%
            
            if any(val != 0 for val in bmc_embedding):
                embeddings.append(np.array(bmc_embedding))
                weights.append(0.4)  # BMC 40%
            
            if not embeddings:
                # 모든 임베딩이 0인 경우
                final_embedding = [0.0] * settings.embedding_dimension
            else:
                # 가중평균 계산
                weights = np.array(weights)
                weights = weights / weights.sum()  # 정규화
                
                weighted_embeddings = [emb * w for emb, w in zip(embeddings, weights)]
                final_embedding = np.sum(weighted_embeddings, axis=0).tolist()
            
            # 메타데이터 생성
            metadata = {
                "user_id": payload.user_profile.user_id,
                "username": payload.user_profile.username,
                "has_introduction": bool(introduction_text.strip()),
                "interest_count": len(payload.user_interests),
                "liked_announcements_count": len(payload.liked_announcements),
                "completed_bmc_count": sum(1 for bmc in payload.bmc_data if bmc.is_completed),
                "business_types": [interest.business_type.value for interest in payload.user_interests]
            }
            
            return {
                "embedding": final_embedding,
                "metadata": metadata
            }
            
        except Exception as e:
            logger.error(f"사용자 임베딩 생성 실패 (user_id: {payload.user_profile.user_id}): {str(e)}")
            raise
    
    async def create_announcement_embedding(self, announcement: AnnouncementData) -> Dict[str, Any]:
        """공고 데이터를 임베딩으로 변환"""
        try:
            # 공고의 주요 정보들을 조합
            text_parts = [
                announcement.title,
                announcement.organization,
                announcement.support_field or "",
                announcement.target_age or "",
                announcement.region or "",
                announcement.content[:1000] if announcement.content else ""  # 내용은 1000자만
            ]
            
            combined_text = " ".join(filter(None, text_parts))
            embedding = await self.get_embedding(combined_text)
            
            # 메타데이터 생성
            metadata = {
                "announcement_id": announcement.id,
                "title": announcement.title,
                "organization": announcement.organization,
                "support_field": announcement.support_field,
                "target_age": announcement.target_age,
                "region": announcement.region,
                "status": announcement.status.value if announcement.status else "ACTIVE"
            }
            
            return {
                "embedding": embedding,
                "metadata": metadata
            }
            
        except Exception as e:
            logger.error(f"공고 임베딩 생성 실패 (announcement_id: {announcement.id}): {str(e)}")
            raise
    
    async def create_batch_embeddings(self, texts: List[str]) -> List[List[float]]:
        """여러 텍스트를 배치로 임베딩 생성 (성능 최적화)"""
        try:
            if not texts:
                return []
            
            # 빈 텍스트 필터링
            valid_texts = [text if text and text.strip() else "빈 내용" for text in texts]
            
            response = await self.client.embeddings.create(
                input=valid_texts,
                model=self.model
            )
            
            return [data.embedding for data in response.data]
            
        except Exception as e:
            logger.error(f"배치 임베딩 생성 실패: {str(e)}")
            # 실패 시 각각 0 벡터로 반환
            return [[0.0] * settings.embedding_dimension for _ in texts]