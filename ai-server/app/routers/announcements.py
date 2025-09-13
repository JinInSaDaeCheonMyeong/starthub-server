from fastapi import APIRouter, HTTPException, BackgroundTasks
from typing import Dict, Any, List
import logging

from ..models.announcement import AnnouncementBatch, AnnouncementData
from ..services.recommendation_service import RecommendationService

logger = logging.getLogger(__name__)
router = APIRouter()

# 서비스 인스턴스
recommendation_service = RecommendationService()


@router.post("/batch")
async def receive_announcements_batch(
    batch: AnnouncementBatch,
    background_tasks: BackgroundTasks
) -> Dict[str, Any]:
    """
    크롤링된 공고들을 배치로 받아 처리
    
    Args:
        batch: 공고 배치 데이터
            - announcements: 공고 리스트
            - batch_id: 배치 식별자
            - timestamp: 배치 생성 시간
    """
    try:
        batch_id = batch.batch_id
        announcements_count = len(batch.announcements)
        logger.info(f"공고 배치 데이터 수신: batch_id={batch_id}, count={announcements_count}")
        
        # 배치가 비어있는지 확인
        if not batch.announcements:
            return {
                "success": True,
                "message": "빈 배치가 수신되었습니다.",
                "batch_id": batch_id,
                "processed_count": 0
            }
        
        # 백그라운드에서 비동기 처리
        background_tasks.add_task(
            recommendation_service.process_announcements_batch,
            batch.announcements
        )
        
        return {
            "success": True,
            "message": f"배치 {batch_id}의 {announcements_count}개 공고가 성공적으로 수신되었습니다. 처리가 진행 중입니다.",
            "batch_id": batch_id,
            "announcements_count": announcements_count,
            "timestamp": batch.timestamp
        }
        
    except Exception as e:
        logger.error(f"공고 배치 처리 중 오류 발생: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"공고 배치 처리 중 오류 발생: {str(e)}"
        )


@router.post("/single")
async def receive_single_announcement(
    announcement: AnnouncementData,
    background_tasks: BackgroundTasks
) -> Dict[str, Any]:
    """
    단일 공고 데이터 처리
    
    Args:
        announcement: 단일 공고 데이터
    """
    try:
        announcement_id = announcement.id
        logger.info(f"단일 공고 데이터 수신: announcement_id={announcement_id}")
        
        # 단일 공고를 리스트로 감싸서 배치 처리 로직 재사용
        announcements_list = [announcement]
        
        background_tasks.add_task(
            recommendation_service.process_announcements_batch,
            announcements_list
        )
        
        return {
            "success": True,
            "message": f"공고 {announcement_id}가 성공적으로 수신되었습니다. 처리가 진행 중입니다.",
            "announcement_id": announcement_id,
            "title": announcement.title
        }
        
    except Exception as e:
        logger.error(f"단일 공고 처리 중 오류 발생: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"공고 처리 중 오류 발생: {str(e)}"
        )


@router.get("/search")
async def search_announcements(
    query: str,
    limit: int = 10,
    filter_active_only: bool = True,
    filter_region: str = None,
    filter_support_field: str = None
) -> Dict[str, Any]:
    """
    키워드 기반 공고 검색
    
    Args:
        query: 검색 키워드
        limit: 검색 결과 수 제한
        filter_active_only: ACTIVE 상태만 검색
        filter_region: 지역 필터
        filter_support_field: 지원분야 필터
    """
    try:
        logger.info(f"공고 검색 요청: query='{query}', limit={limit}")
        
        # 검색 키워드를 임베딩으로 변환
        query_embedding = await recommendation_service.embedding_service.get_embedding(query)
        
        # 필터 설정
        filter_dict = {}
        if filter_active_only:
            filter_dict["status"] = {"$eq": "ACTIVE"}
        if filter_region:
            filter_dict["region"] = {"$eq": filter_region}
        if filter_support_field:
            filter_dict["support_field"] = {"$eq": filter_support_field}
        
        # 유사도 검색
        search_results = await recommendation_service.pinecone_service.find_similar_announcements(
            user_embedding=query_embedding,
            limit=limit,
            filter_dict=filter_dict
        )
        
        return {
            "query": query,
            "results": search_results,
            "total_count": len(search_results),
            "filters_applied": {
                "active_only": filter_active_only,
                "region": filter_region,
                "support_field": filter_support_field
            }
        }
        
    except Exception as e:
        logger.error(f"공고 검색 중 오류 발생: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"공고 검색 중 오류 발생: {str(e)}"
        )


@router.get("/stats")
async def get_announcements_stats() -> Dict[str, Any]:
    """
    공고 관련 통계 정보 조회
    """
    try:
        # Pinecone 통계 정보 조회
        stats = await recommendation_service.get_service_stats()
        
        # 공고 관련 통계만 추출
        announcements_stats = {
            "total_announcements": stats.get("pinecone_stats", {}).get("namespaces", {}).get("announcements", {}).get("vector_count", 0),
            "index_status": stats.get("status", "unknown"),
            "last_updated": stats.get("last_updated")
        }
        
        return announcements_stats
        
    except Exception as e:
        logger.error(f"공고 통계 조회 중 오류 발생: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"통계 조회 중 오류 발생: {str(e)}"
        )


@router.delete("/{announcement_id}")
async def delete_announcement(announcement_id: int) -> Dict[str, Any]:
    """
    특정 공고 데이터 삭제
    
    Args:
        announcement_id: 삭제할 공고 ID
    """
    try:
        logger.info(f"공고 삭제 요청: announcement_id={announcement_id}")
        
        # Pinecone에서 해당 공고 벡터 삭제
        vector_id = f"announcement_{announcement_id}"
        
        if recommendation_service.pinecone_service.index:
            recommendation_service.pinecone_service.index.delete(
                ids=[vector_id],
                namespace="announcements"
            )
        
        return {
            "success": True,
            "message": f"공고 {announcement_id}가 삭제되었습니다.",
            "announcement_id": announcement_id
        }
        
    except Exception as e:
        logger.error(f"공고 삭제 실패 (announcement_id: {announcement_id}): {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"공고 삭제 중 오류 발생: {str(e)}"
        )