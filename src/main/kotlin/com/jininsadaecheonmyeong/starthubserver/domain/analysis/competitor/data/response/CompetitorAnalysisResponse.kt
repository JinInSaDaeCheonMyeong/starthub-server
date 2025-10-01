package com.jininsadaecheonmyeong.starthubserver.domain.analysis.competitor.data.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    title = "경쟁사 분석 응답",
    description = "BMC 기반 경쟁사 분석 결과를 5개 주요 섹션으로 구조화한 데이터",
)
data class CompetitorAnalysisResponse(
    @Schema(description = "사용자 BMC 요약 정보")
    val userBmc: UserBmcSummary,
    @Schema(description = "사용자 규모 분석 결과")
    val userScale: UserScaleAnalysis,
    @Schema(description = "강점 분석 결과")
    val strengths: StrengthsAnalysis,
    @Schema(description = "약점 분석 결과")
    val weaknesses: WeaknessesAnalysis,
    @Schema(description = "글로벌 진출 전략")
    val globalExpansionStrategy: GlobalExpansionStrategy,
)

@Schema(description = "사용자 BMC 요약 정보")
data class UserBmcSummary(
    @Schema(description = "BMC 제목", example = "AI 기반 개인 맞춤형 학습 플랫폼")
    val title: String,
    @Schema(description = "가치 제안", example = "개인의 학습 패턴을 분석하여 최적화된 학습 경험 제공")
    val valueProposition: String?,
    @Schema(description = "타겟 고객", example = "20-30대 직장인, 대학생")
    val targetCustomer: String?,
    @Schema(description = "핵심 강점 목록", example = "[\"명확한 가치 제안\", \"핵심 자원 보유\", \"수익 모델 확립\"]")
    val keyStrengths: List<String>,
)

@Schema(description = "사용자 규모 분석 결과")
data class UserScaleAnalysis(
    @Schema(description = "예상 사용자 기반 규모", example = "초기 단계 (1,000-5,000명)")
    val estimatedUserBase: String,
    @Schema(description = "시장 내 위치", example = "신규 진입자")
    val marketPosition: String,
    @Schema(description = "성장 잠재력", example = "높은 성장 가능성 (월 30% 성장률 기대)")
    val growthPotential: String,
    @Schema(description = "경쟁사와의 규모 비교")
    val competitorComparison: List<CompetitorScale>,
)

@Schema(description = "경쟁사 규모 정보")
data class CompetitorScale(
    @Schema(description = "경쟁사명", example = "클래스101")
    val name: String,
    @Schema(description = "로고 URL", example = "https://class101.net/logo.png")
    val logoUrl: String?,
    @Schema(description = "웹사이트 URL", example = "https://class101.net")
    val websiteUrl: String?,
    @Schema(description = "예상 규모", example = "대규모 (100만명+)")
    val estimatedScale: String,
    @Schema(description = "시장 점유율", example = "25-30%")
    val marketShare: String,
    @Schema(description = "우리 서비스와의 유사점 (2-3개)", example = "[\"온라인 교육 플랫폼\", \"구독 기반 수익 모델\"]")
    val similarities: List<String> = emptyList(),
    @Schema(description = "우리 서비스와의 차이점 (2-3개)", example = "[\"취미/창작 중심 vs AI 기반 학습\", \"크리에이터 중심 vs 학습자 중심\"]")
    val differences: List<String> = emptyList(),
)

@Schema(description = "강점 분석 결과")
data class StrengthsAnalysis(
    @Schema(description = "경쟁 우위 요소 (4개)", example = "[\"AI 기반 개인화 학습 알고리즘\", \"실시간 학습 진도 추적 시스템\"]")
    val competitiveAdvantages: List<String>,
    @Schema(description = "고유한 가치 제안 (3개)", example = "[\"개인 맞춤형 학습 경로 자동 생성\", \"학습 효과 실시간 측정 및 피드백\"]")
    val uniqueValuePropositions: List<String>,
    @Schema(description = "시장 기회 요소 (4개)", example = "[\"원격 학습 시장 급성장\", \"개인화 교육 수요 증가\"]")
    val marketOpportunities: List<String>,
    @Schema(description = "전략적 권고사항 (4개)", example = "[\"초기 사용자 확보를 위한 무료 체험 기간 확대\", \"인플루언서 마케팅을 통한 브랜드 인지도 향상\"]")
    val strategicRecommendations: List<String>,
)

@Schema(description = "약점 분석 결과")
data class WeaknessesAnalysis(
    @Schema(description = "경쟁 열위 요소 (4개)", example = "[\"브랜드 인지도 부족으로 인한 신뢰성 이슈\", \"콘텐츠 양과 질에서 기존 플랫폼 대비 부족\"]")
    val competitiveDisadvantages: List<String>,
    @Schema(description = "시장 도전 과제 (4개)", example = "[\"경쟁이 심화되고 있는 에듀테크 시장\", \"대형 플랫폼들의 가격 경쟁력\"]")
    val marketChallenges: List<String>,
    @Schema(description = "자원 제약 사항 (3개)", example = "[\"초기 단계로 인한 자금 조달의 한계\", \"개발 인력 및 전문가 확보 어려움\"]")
    val resourceLimitations: List<String>,
    @Schema(description = "개선 필요 영역 (4개)", example = "[\"사용자 경험(UX) 개선 및 최적화\", \"학습 콘텐츠의 다양성과 전문성 강화\"]")
    val improvementAreas: List<String>,
)

@Schema(description = "경쟁사 상세 정보")
data class CompetitorInfo(
    @Schema(description = "경쟁사명", example = "클래스101")
    val name: String,
    @Schema(description = "경쟁사 설명", example = "온라인 클래스 플랫폼")
    val description: String,
    @Schema(description = "로고 URL", example = "https://class101.net/logo.png")
    val logoUrl: String?,
    @Schema(description = "웹사이트 URL", example = "https://class101.net")
    val websiteUrl: String?,
    @Schema(description = "가치 제안", example = "크리에이터와 함께하는 취미 클래스")
    val valueProposition: String = "",
    @Schema(description = "강점 목록", example = "[\"브랜드 인지도\", \"다양한 콘텐츠\"]")
    val strengths: List<String> = emptyList(),
    @Schema(description = "약점 목록", example = "[\"높은 가격\", \"진입 장벽\"]")
    val weaknesses: List<String> = emptyList(),
    @Schema(description = "시장 위치", example = "시장 리더")
    val marketPosition: String = "",
)

@Schema(description = "글로벌 진출 전략")
data class GlobalExpansionStrategy(
    @Schema(description = "우선 진출 시장 (3개)", example = "[\"동남아시아 (베트남, 태국)\", \"북미 (미국, 캐나다)\", \"유럽 (영국, 독일)\"]")
    val priorityMarkets: List<String>,
    @Schema(description = "시장별 진입 전략 (3개)", example = "[\"동남아: 모바일 우선 전략과 현지 언어 지원\", \"북미: 프리미엄 포지셔닝과 B2B 파트너십\"]")
    val entryStrategies: List<String>,
    @Schema(description = "현지화 요구사항 (3-4개)", example = "[\"다국어 지원 (영어, 중국어, 일본어 우선)\", \"현지 결제 시스템 통합\"]")
    val localizationRequirements: List<String>,
    @Schema(description = "글로벌 파트너십 기회 (3-4개)", example = "[\"현지 교육 기관과의 제휴\", \"글로벌 클라우드 서비스 제공업체 협력\"]")
    val partnershipOpportunities: List<String>,
    @Schema(description = "예상 도전 과제 (3개)", example = "[\"문화적 차이로 인한 학습 방식 선호도 차이\", \"현지 경쟁사의 시장 선점\"]")
    val expectedChallenges: List<String>,
)
