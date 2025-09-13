from fastapi import APIRouter, HTTPException, BackgroundTasks
from typing import Dict, Any
import logging

from ..models.user import UserInterestsPayload
from ..models.announcement import RecommendationRequest, RecommendationResponse
from ..services.recommendation_service import RecommendationService

logger = logging.getLogger(__name__)
router = APIRouter()

# 서비스 인스턴스 (싱글톤 패턴으로 관리하는 것이 좋지만, 일단 간단하게)
recommendation_service = RecommendationService()


@router.post("/interests")
async def receive_user_interests(
    payload: UserInterestsPayload,
    background_tasks: BackgroundTasks
) -> Dict[str, Any]:
    """
    Spring Boot 서버에서 사용자별 관심사 데이터를 받아 처리
    
    - user_profile: 사용자 프로필 정보 (자기소개 포함)
    - user_interests: 회원가입 시 선택한 관심 분야
    - liked_announcements: 좋아요 표시한 공고들
    - bmc_data: 작성한 BMC 데이터
    """
    try:
        user_id = payload.user_profile.user_id
        logger.info(f"사용자 관심사 데이터 수신: user_id={user_id}")
        
        # 백그라운드에서 비동기 처리 (응답 속도 향상)
        background_tasks.add_task(
            recommendation_service.process_user_interests,
            payload
        )
        
        return {
            "success": True,
            "message": f"사용자 {user_id}의 관심사 데이터가 성공적으로 수신되었습니다. 처리가 진행 중입니다.",
            "user_id": user_id,
            "data_summary": {
                "interests_count": len(payload.user_interests),
                "liked_announcements_count": len(payload.liked_announcements),
                "bmc_count": len(payload.bmc_data)
            }
        }
        
    except Exception as e:
        logger.error(f"사용자 관심사 데이터 처리 중 오류 발생: {str(e)}")
        raise HTTPException(
            status_code=500, 
            detail=f"데이터 처리 중 오류 발생: {str(e)}"
        )


@router.get("/{user_id}/recommendations")
async def get_user_recommendations(
    user_id: int,
    page: int = 1,
    page_size: int = 10,
    filter_active_only: bool = True,
    filter_region: str = None,
    filter_support_field: str = None
) -> Dict[str, Any]:
    """
    사용자별 관심사에 맞는 공고 추천 (페이지네이션 포함)
    
    Args:
        user_id: 사용자 ID
        page: 페이지 번호 (기본값: 1)
        page_size: 페이지당 항목 수 (기본값: 10)
        filter_active_only: ACTIVE 상태 공고만 필터링 (기본값: True)
        filter_region: 지역 필터 (선택사항)
        filter_support_field: 지원분야 필터 (선택사항)
    """
    try:
        logger.info(f"사용자 추천 요청: user_id={user_id}, page={page}, page_size={page_size}")
        
        # 입력 값 검증
        if page < 1:
            raise HTTPException(status_code=400, detail="페이지 번호는 1 이상이어야 합니다.")
        if page_size < 1 or page_size > 50:
            raise HTTPException(status_code=400, detail="페이지 크기는 1~50 사이여야 합니다.")
        
        # 추천 요청 객체 생성
        request = RecommendationRequest(
            user_id=user_id,
            page=page,
            page_size=page_size,
            filter_active_only=filter_active_only,
            filter_region=filter_region,
            filter_support_field=filter_support_field
        )
        
        # 추천 생성
        recommendations = await recommendation_service.generate_recommendations(request)
        
        return recommendations
        
    except Exception as e:
        logger.error(f"사용자 추천 생성 중 오류 발생 (user_id: {user_id}): {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"추천 생성 중 오류 발생: {str(e)}"
        )


@router.post("/{user_id}/interactions")
async def record_user_interaction(
    user_id: int,
    announcement_id: int,
    interaction_type: str
) -> Dict[str, Any]:
    """
    사용자 상호작용 기록 (클릭, 좋아요, 지원 등)
    향후 실시간 학습에 활용
    
    Args:
        user_id: 사용자 ID
        announcement_id: 공고 ID
        interaction_type: 상호작용 유형 (click, like, apply, view 등)
    """
    try:
        logger.info(f"사용자 상호작용 기록: user_id={user_id}, announcement_id={announcement_id}, type={interaction_type}")
        
        success = await recommendation_service.update_user_interaction(
            user_id=user_id,
            announcement_id=announcement_id,
            interaction_type=interaction_type
        )
        
        if success:
            return {
                "success": True,
                "message": "사용자 상호작용이 기록되었습니다.",
                "user_id": user_id,
                "announcement_id": announcement_id,
                "interaction_type": interaction_type
            }
        else:
            raise HTTPException(status_code=500, detail="상호작용 기록 실패")
            
    except Exception as e:
        logger.error(f"사용자 상호작용 기록 실패: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"상호작용 기록 중 오류 발생: {str(e)}"
        )


@router.delete("/{user_id}/data")
async def delete_user_data(user_id: int) -> Dict[str, Any]:
    """
    사용자 데이터 삭제 (GDPR 준수)
    
    Args:
        user_id: 삭제할 사용자 ID
    """
    try:
        logger.info(f"사용자 데이터 삭제 요청: user_id={user_id}")
        
        success = await recommendation_service.pinecone_service.delete_user_data(user_id)
        
        if success:
            return {
                "success": True,
                "message": f"사용자 {user_id}의 데이터가 삭제되었습니다.",
                "user_id": user_id
            }
        else:
            raise HTTPException(status_code=500, detail="데이터 삭제 실패")
            
    except Exception as e:
        logger.error(f"사용자 데이터 삭제 실패 (user_id: {user_id}): {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"데이터 삭제 중 오류 발생: {str(e)}"
        )