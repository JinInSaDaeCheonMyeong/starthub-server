from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import logging
from typing import Dict, Any
import uvicorn

from .core.config import settings
from .routers import users, announcements
from .services.recommendation_service import RecommendationService

# 로깅 설정
logging.basicConfig(
    level=getattr(logging, settings.log_level.upper()),
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 전역 서비스 인스턴스 (앱 시작 시 초기화)
recommendation_service: RecommendationService = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """애플리케이션 라이프사이클 관리"""
    # 시작 시
    logger.info("StartHub AI 서버 시작")
    
    try:
        # 서비스 초기화
        global recommendation_service
        recommendation_service = RecommendationService()
        
        # Pinecone 연결 테스트
        if recommendation_service.pinecone_service.health_check():
            logger.info("Pinecone 연결 성공")
        else:
            logger.warning("Pinecone 연결 실패 - 일부 기능이 제한될 수 있습니다")
        
        logger.info("서비스 초기화 완료")
        
    except Exception as e:
        logger.error(f"서비스 초기화 실패: {str(e)}")
        # 초기화 실패 시에도 서버를 시작하되, 상태를 기록
    
    yield
    
    # 종료 시
    logger.info("StartHub AI 서버 종료")


# FastAPI 애플리케이션 생성
app = FastAPI(
    title="StartHub AI Recommendation Service",
    description="사용자 맞춤형 창업 지원 공고 추천 시스템",
    version="1.0.0",
    lifespan=lifespan
)

# CORS 미들웨어 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 개발 환경용, 프로덕션에서는 제한 필요
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 라우터 등록
app.include_router(
    users.router,
    prefix="/api/v1/users",
    tags=["users"]
)

app.include_router(
    announcements.router,
    prefix="/api/v1/announcements",
    tags=["announcements"]
)


@app.get("/")
async def root() -> Dict[str, Any]:
    """루트 엔드포인트 - 서비스 상태 확인"""
    return {
        "service": "StartHub AI Recommendation Service",
        "version": "1.0.0",
        "status": "running",
        "description": "사용자 맞춤형 창업 지원 공고 추천 시스템"
    }


@app.get("/health")
async def health_check() -> Dict[str, Any]:
    """헬스체크 엔드포인트"""
    try:
        health_status = {
            "status": "healthy",
            "service": "StartHub AI Service",
            "timestamp": None
        }
        
        # Pinecone 연결 상태 확인
        if recommendation_service and recommendation_service.pinecone_service:
            pinecone_status = recommendation_service.pinecone_service.health_check()
            health_status["pinecone"] = "connected" if pinecone_status else "disconnected"
        else:
            health_status["pinecone"] = "not_initialized"
        
        # 서비스 통계 정보
        if recommendation_service:
            stats = await recommendation_service.get_service_stats()
            health_status["stats"] = stats
            health_status["timestamp"] = stats.get("last_updated")
        
        return health_status
        
    except Exception as e:
        logger.error(f"헬스체크 실패: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"헬스체크 실패: {str(e)}"
        )


@app.get("/api/v1/stats")
async def get_service_stats() -> Dict[str, Any]:
    """서비스 전체 통계 정보"""
    try:
        if not recommendation_service:
            raise HTTPException(
                status_code=503,
                detail="서비스가 초기화되지 않았습니다"
            )
        
        stats = await recommendation_service.get_service_stats()
        return stats
        
    except Exception as e:
        logger.error(f"통계 조회 실패: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"통계 조회 실패: {str(e)}"
        )


@app.exception_handler(Exception)
async def global_exception_handler(request, exc):
    """전역 예외 처리기"""
    logger.error(f"예상치 못한 오류 발생: {str(exc)}")
    return HTTPException(
        status_code=500,
        detail="내부 서버 오류가 발생했습니다."
    )


if __name__ == "__main__":
    # 개발 서버 실행
    uvicorn.run(
        "app.main:app",
        host=settings.fastapi_host,
        port=settings.fastapi_port,
        reload=settings.fastapi_debug,
        log_level=settings.log_level.lower()
    )