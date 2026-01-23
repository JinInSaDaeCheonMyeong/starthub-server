package com.jininsadaecheonmyeong.starthubserver.global.infra.ai

object ClaudePromptTemplates {
    fun buildStartupAssistantPrompt(userContext: String): String {
        return """
            당신은 StartHub의 AI 스타트업 전문 어시스턴트입니다.

            ## 역할
            - 스타트업 창업자들에게 맞춤형 조언 제공
            - 정부 지원사업, 법률, 규제 관련 최신 정보 안내
            - 비즈니스 모델 캔버스(BMC) 분석 및 개선 제안
            - 경쟁사 분석 및 시장 전략 조언
            - 관련 지원 공고 추천 및 안내

            ## 사용자 맥락
            $userContext

            ## 지침
            1. 한국어로 친근하고 전문적으로 응답하세요
            2. 사용자의 BMC와 관심사를 고려한 맞춤형 답변을 제공하세요
            3. 법률/규제 관련 질문은 반드시 최신 정보를 기반으로 답변하세요
            4. 불확실한 정보는 명확히 표시하고, 전문가 상담을 권고하세요
            5. 관련 정부 지원사업이 있다면 적극적으로 안내하세요
            6. 응답은 명확하고 구조적으로 작성하세요
            7. 실행 가능한 조언과 구체적인 다음 단계를 제시하세요
            """.trimIndent()
    }

    val WEB_SEARCH_KEYWORDS =
        listOf(
            "법률",
            "법",
            "규제",
            "법적",
            "세금",
            "세무",
            "최신",
            "현재",
            "2024년",
            "2025년",
            "2026년",
            "정책",
            "정부",
            "지원금",
            "보조금",
            "인허가",
            "특허",
            "상표",
            "저작권",
            "노동법",
            "근로기준법",
            "4대보험",
            "창업지원",
            "벤처",
            "투자",
        )

    val ANNOUNCEMENT_KEYWORDS =
        listOf(
            "공고",
            "지원사업",
            "지원금",
            "보조금",
            "정부지원",
            "창업지원",
            "기업지원",
            "R&D",
            "연구개발",
            "모집",
            "신청",
            "접수",
            "프로그램",
            "사업",
            "펀딩",
        )
}
