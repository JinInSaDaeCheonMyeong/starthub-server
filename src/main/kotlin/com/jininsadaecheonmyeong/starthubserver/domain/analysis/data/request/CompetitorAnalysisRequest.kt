package com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(
    title = "경쟁사 분석 요청",
    description = "BMC 기반 경쟁사 분석을 위한 요청 데이터",
)
data class CompetitorAnalysisRequest(
    @field:NotNull(message = "BMC ID는 필수입니다.")
    @Schema(
        description = "분석할 BMC의 고유 ID",
        example = "1",
        required = true,
    )
    val bmcId: Long,
    @Schema(
        description = "특정 검색 키워드 (선택사항). 미지정 시 BMC 내용 기반으로 자동 생성",
        example = "[\"AI 학습\", \"온라인 교육\", \"에듀테크\"]",
        nullable = true,
    )
    val searchKeywords: List<String>? = null,
)
