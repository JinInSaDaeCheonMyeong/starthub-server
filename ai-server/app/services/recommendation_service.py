import logging
from typing import List, Dict, Any, Optional
from datetime import datetime

from .embedding_service import EmbeddingService
from .pinecone_service import PineconeService
from ..models.user import UserInterestsPayload
from ..models.announcement import AnnouncementData, RecommendationRequest

logger = logging.getLogger(__name__)


class RecommendationService:
    def __init__(self):
        self.embedding_service = EmbeddingService()
        self.pinecone_service = PineconeService()
    
    async def process_user_interests(self, payload: UserInterestsPayload) -> bool:
        """사용자 관심사 데이터를 처리하여 Pinecone에 저장"""
        try:
            user_id = payload.user_profile.user_id
            logger.info(f"사용자 관심사 처리 시작: user_id={user_id}")
            
            # 1. 사용자 데이터를 임베딩으로 변환
            embedding_data = await self.embedding_service.create_user_interest_embedding(payload)
            
            # 2. Pinecone에 저장
            success = await self.pinecone_service.store_user_interests(
                user_id=user_id,
                embedding=embedding_data["embedding"],
                metadata=embedding_data["metadata"]
            )
            
            if success:
                logger.info(f"사용자 관심사 처리 완료: user_id={user_id}")
            else:
                logger.error(f"사용자 관심사 저장 실패: user_id={user_id}")
            
            return success
            
        except Exception as e:
            logger.error(f"사용자 관심사 처리 실패: {str(e)}")
            return False
    
    async def process_announcements_batch(self, announcements: List[AnnouncementData]) -> Dict[str, Any]:
        """공고들을 배치로 처리하여 Pinecone에 저장"""
        try:
            logger.info(f"공고 배치 처리 시작: {len(announcements)}개")
            
            if not announcements:
                return {"success": True, "processed_count": 0, "total_count": 0}
            
            # 1. 모든 공고를 임베딩으로 변환
            embeddings_data = []
            
            for announcement in announcements:
                try:
                    embedding_data = await self.embedding_service.create_announcement_embedding(announcement)
                    embeddings_data.append(embedding_data)
                except Exception as e:
                    logger.error(f"공고 임베딩 생성 실패 (id: {announcement.id}): {str(e)}")
                    continue
            
            # 2. Pinecone에 배치 저장
            success_count = await self.pinecone_service.store_announcements_batch(embeddings_data)
            
            result = {
                "success": success_count > 0,
                "processed_count": success_count,
                "total_count": len(announcements),
                "failed_count": len(announcements) - success_count
            }
            
            logger.info(f"공고 배치 처리 완료: {success_count}/{len(announcements)}개 성공")
            return result
            
        except Exception as e:
            logger.error(f"공고 배치 처리 실패: {str(e)}")
            return {
                "success": False,
                "processed_count": 0,
                "total_count": len(announcements),
                "error": str(e)
            }
    
    async def generate_recommendations(self, request: RecommendationRequest) -> Dict[str, Any]:
        """사용자별 맞춤 공고 추천 생성 (페이지네이션 포함)"""
        try:
            user_id = request.user_id
            logger.info(f"추천 생성 시작: user_id={user_id}, page={request.page}")
            
            # 1. 사용자 임베딩 조회
            user_embedding = await self.pinecone_service.get_user_embedding(user_id)
            
            if not user_embedding:
                logger.warning(f"사용자 임베딩을 찾을 수 없습니다: user_id={user_id}")
                return self._create_empty_recommendation_response(request, "사용자 관심사 데이터를 찾을 수 없습니다.")
            
            # 2. 필터 설정 (접수기간 체크 포함)
            filter_dict = await self._build_filter_dict(request)
            
            # 3. 더 많은 결과를 가져와서 페이지네이션 적용 (성능 최적화를 위해 제한)
            search_limit = min(request.page * request.page_size + 50, 200)
            
            # 4. 유사한 공고 검색
            recommendations = await self.pinecone_service.find_similar_announcements(
                user_embedding=user_embedding,
                limit=search_limit,
                filter_dict=filter_dict
            )
            
            # 5. 추천 결과 후처리 및 접수기간 재확인
            processed_recommendations = await self._post_process_recommendations_with_dates(recommendations)
            
            # 6. 페이지네이션 적용
            paginated_result = self._apply_pagination(processed_recommendations, request)
            
            logger.info(f"추천 생성 완료: user_id={user_id}, {len(paginated_result['recommendations'])}개 추천 (총 {paginated_result['total_count']}개)")
            return paginated_result
            
        except Exception as e:
            logger.error(f"추천 생성 실패 (user_id: {request.user_id}): {str(e)}")
            return self._create_empty_recommendation_response(request, str(e))
    
    def _create_empty_recommendation_response(self, request: RecommendationRequest, error_message: str = None) -> Dict[str, Any]:
        """빈 추천 응답 생성"""
        return {
            "user_id": request.user_id,
            "recommendations": [],
            "total_count": 0,
            "page": request.page,
            "page_size": request.page_size,
            "total_pages": 0,
            "has_next": False,
            "has_previous": False,
            "generated_at": datetime.now().isoformat(),
            "error": error_message
        }
    
    async def _build_filter_dict(self, request: RecommendationRequest) -> Dict[str, Any]:
        """필터 딕셔너리 생성"""
        filter_dict = {}
        
        if request.filter_active_only:
            filter_dict["status"] = {"$eq": "ACTIVE"}
        
        if request.filter_region:
            filter_dict["region"] = {"$eq": request.filter_region}
        
        if request.filter_support_field:
            filter_dict["support_field"] = {"$eq": request.filter_support_field}
        
        return filter_dict
    
    async def _post_process_recommendations_with_dates(self, recommendations: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """추천 결과 후처리 및 접수기간 확인"""
        try:
            if not recommendations:
                return []
            
            # 현재 날짜
            from datetime import date
            today = date.today()
            
            processed = []
            org_count = {}
            
            for rec in recommendations:
                try:
                    # 접수기간 확인 (Spring Boot와 동일한 로직)
                    reception_period = rec.get("metadata", {}).get("reception_period", "")
                    if reception_period and "~" in reception_period:
                        try:
                            end_date_str = reception_period.split("~")[1].strip()
                            end_date = datetime.strptime(end_date_str, "%Y-%m-%d").date()
                            
                            # 접수기간이 지난 공고는 제외
                            if end_date < today:
                                continue
                        except (ValueError, IndexError):
                            # 날짜 파싱 실패 시 일단 포함 (안전한 방향)
                            pass
                    
                    # 기관별 중복 제한
                    org = rec.get("organization", "")
                    current_count = org_count.get(org, 0)
                    
                    if current_count < 3:  # 같은 기관 최대 3개
                        # 유사도 점수 포맷팅
                        if "similarity_score" in rec:
                            rec["similarity_score"] = round(rec["similarity_score"], 3)
                        
                        # announcement_id 추가 (클라이언트에서 필요)
                        rec["announcement_id"] = rec.get("metadata", {}).get("announcement_id")
                        
                        processed.append(rec)
                        org_count[org] = current_count + 1
                
                except Exception as e:
                    logger.warning(f"추천 항목 처리 중 오류: {str(e)}")
                    continue
            
            # 유사도 점수 기준으로 정렬
            processed.sort(key=lambda x: x.get("similarity_score", 0), reverse=True)
            
            return processed
            
        except Exception as e:
            logger.error(f"추천 후처리 실패: {str(e)}")
            return recommendations
    
    def _apply_pagination(self, recommendations: List[Dict[str, Any]], request: RecommendationRequest) -> Dict[str, Any]:
        """페이지네이션 적용"""
        total_count = len(recommendations)
        total_pages = (total_count + request.page_size - 1) // request.page_size
        
        start_idx = (request.page - 1) * request.page_size
        end_idx = start_idx + request.page_size
        
        paginated_recommendations = recommendations[start_idx:end_idx]
        
        return {
            "user_id": request.user_id,
            "recommendations": paginated_recommendations,
            "total_count": total_count,
            "page": request.page,
            "page_size": request.page_size,
            "total_pages": total_pages,
            "has_next": request.page < total_pages,
            "has_previous": request.page > 1,
            "generated_at": datetime.now().isoformat(),
            "filters_applied": {
                "active_only": request.filter_active_only,
                "region": request.filter_region,
                "support_field": request.filter_support_field
            }
        }
    
    def _post_process_recommendations(self, recommendations: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """추천 결과 후처리"""
        try:
            if not recommendations:
                return []
            
            # 1. 중복 제거 (같은 기관의 공고가 너무 많지 않도록)
            processed = []
            org_count = {}
            
            for rec in recommendations:
                org = rec.get("organization", "")
                current_count = org_count.get(org, 0)
                
                # 같은 기관의 공고는 최대 3개까지만
                if current_count < 3:
                    processed.append(rec)
                    org_count[org] = current_count + 1
            
            # 2. 유사도 점수 기준으로 정렬
            processed.sort(key=lambda x: x.get("similarity_score", 0), reverse=True)
            
            # 3. 추가 메타데이터 정리
            for rec in processed:
                # 소수점 둘째자리까지만 표시
                if "similarity_score" in rec:
                    rec["similarity_score"] = round(rec["similarity_score"], 2)
                
                # 불필요한 메타데이터 제거
                if "metadata" in rec:
                    unnecessary_fields = ["updated_at", "type"]
                    for field in unnecessary_fields:
                        rec["metadata"].pop(field, None)
            
            return processed
            
        except Exception as e:
            logger.error(f"추천 후처리 실패: {str(e)}")
            return recommendations
    
    async def update_user_interaction(
        self, 
        user_id: int, 
        announcement_id: int, 
        interaction_type: str
    ) -> bool:
        """사용자 상호작용 기반으로 관심사 업데이트 (향후 확장용)"""
        try:
            # TODO: 실시간 학습 로직 구현
            # 사용자가 클릭, 좋아요, 지원 등의 행동을 했을 때
            # 해당 공고의 특성을 사용자 임베딩에 반영
            
            logger.info(f"사용자 상호작용 기록: user_id={user_id}, announcement_id={announcement_id}, type={interaction_type}")
            
            # 현재는 로깅만 하고, 향후 실시간 학습 기능 추가 예정
            return True
            
        except Exception as e:
            logger.error(f"사용자 상호작용 업데이트 실패: {str(e)}")
            return False
    
    async def get_service_stats(self) -> Dict[str, Any]:
        """서비스 통계 정보 조회"""
        try:
            # Pinecone 통계 정보
            pinecone_stats = await self.pinecone_service.get_index_stats()
            
            # 서비스 상태 정보
            service_stats = {
                "service_name": "StartHub AI Recommendation Service",
                "status": "healthy" if self.pinecone_service.health_check() else "unhealthy",
                "pinecone_stats": pinecone_stats,
                "last_updated": datetime.now().isoformat()
            }
            
            return service_stats
            
        except Exception as e:
            logger.error(f"서비스 통계 조회 실패: {str(e)}")
            return {
                "service_name": "StartHub AI Recommendation Service",
                "status": "error",
                "error": str(e),
                "last_updated": datetime.now().isoformat()
            }