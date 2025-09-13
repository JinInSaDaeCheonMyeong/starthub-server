import logging
import asyncio
from typing import List, Dict, Any, Optional
from datetime import datetime
import pinecone
from pinecone import Pinecone, Index

from ..core.config import settings
from ..models.user import UserInterestsPayload
from ..models.announcement import AnnouncementData

logger = logging.getLogger(__name__)


class PineconeService:
    def __init__(self):
        self.pc = Pinecone(api_key=settings.pinecone_api_key)
        self.index_name = settings.pinecone_index_name
        self.index: Optional[Index] = None
        self._initialize_index()
    
    def _initialize_index(self):
        """Pinecone 인덱스 초기화"""
        try:
            # 기존 인덱스 목록 확인
            existing_indexes = [index_info["name"] for index_info in self.pc.list_indexes()]
            
            if self.index_name not in existing_indexes:
                # 인덱스가 없으면 생성
                logger.info(f"Pinecone 인덱스 생성: {self.index_name}")
                self.pc.create_index(
                    name=self.index_name,
                    dimension=settings.embedding_dimension,
                    metric="cosine",
                    spec=pinecone.ServerlessSpec(
                        cloud="aws",
                        region="us-east-1"
                    )
                )
                # 인덱스 생성 완료까지 대기
                while self.index_name not in [index_info["name"] for index_info in self.pc.list_indexes()]:
                    asyncio.sleep(1)
            
            # 인덱스 연결
            self.index = self.pc.Index(self.index_name)
            logger.info(f"Pinecone 인덱스 연결 완료: {self.index_name}")
            
        except Exception as e:
            logger.error(f"Pinecone 인덱스 초기화 실패: {str(e)}")
            raise
    
    async def store_user_interests(self, user_id: int, embedding: List[float], metadata: Dict[str, Any]) -> bool:
        """사용자 관심사 임베딩을 Pinecone에 저장"""
        try:
            if not self.index:
                raise Exception("Pinecone 인덱스가 초기화되지 않았습니다")
            
            # 사용자별 고유 ID 생성
            vector_id = f"user_{user_id}"
            
            # 메타데이터에 업데이트 시간 추가
            metadata.update({
                "updated_at": datetime.now().isoformat(),
                "type": "user_interest"
            })
            
            # Pinecone에 upsert (없으면 생성, 있으면 업데이트)
            self.index.upsert(
                vectors=[{
                    "id": vector_id,
                    "values": embedding,
                    "metadata": metadata
                }],
                namespace="users"
            )
            
            logger.info(f"사용자 관심사 저장 완료: user_id={user_id}")
            return True
            
        except Exception as e:
            logger.error(f"사용자 관심사 저장 실패 (user_id: {user_id}): {str(e)}")
            return False
    
    async def store_announcements_batch(self, announcements_data: List[Dict[str, Any]]) -> int:
        """공고들을 배치로 Pinecone에 저장"""
        try:
            if not self.index:
                raise Exception("Pinecone 인덱스가 초기화되지 않았습니다")
            
            if not announcements_data:
                return 0
            
            # Pinecone 배치 처리를 위한 벡터 리스트 생성
            vectors_to_upsert = []
            
            for ann_data in announcements_data:
                vector_id = f"announcement_{ann_data['metadata']['announcement_id']}"
                
                # 메타데이터에 업데이트 시간 추가
                ann_data['metadata'].update({
                    "updated_at": datetime.now().isoformat(),
                    "type": "announcement"
                })
                
                vectors_to_upsert.append({
                    "id": vector_id,
                    "values": ann_data['embedding'],
                    "metadata": ann_data['metadata']
                })
            
            # 배치 크기로 나누어 처리 (Pinecone 제한 고려)
            batch_size = 100
            success_count = 0
            
            for i in range(0, len(vectors_to_upsert), batch_size):
                batch = vectors_to_upsert[i:i + batch_size]
                
                try:
                    self.index.upsert(
                        vectors=batch,
                        namespace="announcements"
                    )
                    success_count += len(batch)
                    
                except Exception as batch_error:
                    logger.error(f"배치 저장 실패 (batch {i//batch_size + 1}): {str(batch_error)}")
                    continue
            
            logger.info(f"공고 배치 저장 완료: {success_count}/{len(announcements_data)}개")
            return success_count
            
        except Exception as e:
            logger.error(f"공고 배치 저장 실패: {str(e)}")
            return 0
    
    async def find_similar_announcements(
        self, 
        user_embedding: List[float], 
        limit: int = 10,
        filter_dict: Optional[Dict[str, Any]] = None
    ) -> List[Dict[str, Any]]:
        """사용자 관심사와 유사한 공고들 검색"""
        try:
            if not self.index:
                raise Exception("Pinecone 인덱스가 초기화되지 않았습니다")
            
            # 기본 필터 설정
            base_filter = {"type": {"$eq": "announcement"}}
            
            # 추가 필터 적용
            if filter_dict:
                base_filter.update(filter_dict)
            
            # 유사도 검색
            search_result = self.index.query(
                vector=user_embedding,
                top_k=limit,
                include_metadata=True,
                namespace="announcements",
                filter=base_filter
            )
            
            # 결과 포맷팅
            recommendations = []
            for match in search_result.matches:
                recommendation = {
                    "announcement_id": match.metadata.get("announcement_id"),
                    "title": match.metadata.get("title"),
                    "organization": match.metadata.get("organization"),
                    "support_field": match.metadata.get("support_field"),
                    "region": match.metadata.get("region"),
                    "similarity_score": float(match.score),
                    "metadata": match.metadata
                }
                recommendations.append(recommendation)
            
            logger.info(f"유사 공고 검색 완료: {len(recommendations)}개 결과")
            return recommendations
            
        except Exception as e:
            logger.error(f"유사 공고 검색 실패: {str(e)}")
            return []
    
    async def get_user_embedding(self, user_id: int) -> Optional[List[float]]:
        """사용자 임베딩 조회"""
        try:
            if not self.index:
                raise Exception("Pinecone 인덱스가 초기화되지 않았습니다")
            
            vector_id = f"user_{user_id}"
            
            # 특정 벡터 조회
            fetch_result = self.index.fetch(
                ids=[vector_id],
                namespace="users"
            )
            
            if vector_id in fetch_result.vectors:
                return fetch_result.vectors[vector_id].values
            else:
                logger.warning(f"사용자 임베딩을 찾을 수 없습니다: user_id={user_id}")
                return None
                
        except Exception as e:
            logger.error(f"사용자 임베딩 조회 실패 (user_id: {user_id}): {str(e)}")
            return None
    
    async def delete_user_data(self, user_id: int) -> bool:
        """사용자 데이터 삭제"""
        try:
            if not self.index:
                raise Exception("Pinecone 인덱스가 초기화되지 않았습니다")
            
            vector_id = f"user_{user_id}"
            
            self.index.delete(
                ids=[vector_id],
                namespace="users"
            )
            
            logger.info(f"사용자 데이터 삭제 완료: user_id={user_id}")
            return True
            
        except Exception as e:
            logger.error(f"사용자 데이터 삭제 실패 (user_id: {user_id}): {str(e)}")
            return False
    
    async def get_index_stats(self) -> Dict[str, Any]:
        """인덱스 통계 정보 조회"""
        try:
            if not self.index:
                raise Exception("Pinecone 인덱스가 초기화되지 않았습니다")
            
            stats = self.index.describe_index_stats()
            
            return {
                "total_vectors": stats.total_vector_count,
                "namespaces": stats.namespaces,
                "dimension": stats.dimension,
                "index_fullness": stats.index_fullness
            }
            
        except Exception as e:
            logger.error(f"인덱스 통계 조회 실패: {str(e)}")
            return {}
    
    def health_check(self) -> bool:
        """Pinecone 연결 상태 확인"""
        try:
            if not self.index:
                return False
            
            # 간단한 쿼리로 연결 상태 확인
            self.index.describe_index_stats()
            return True
            
        except Exception:
            return False