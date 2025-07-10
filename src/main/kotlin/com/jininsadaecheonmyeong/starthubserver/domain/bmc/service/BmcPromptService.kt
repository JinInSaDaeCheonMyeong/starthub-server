package com.jininsadaecheonmyeong.starthubserver.domain.bmc.service

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BmcQuestion
import org.springframework.stereotype.Service

@Service
class BmcPromptService {
    fun generateBmcPrompt(bmcQuestion: BmcQuestion): String {
        val questions =
            listOf(
                "사업 아이디어로 해결하고자 하는 핵심 문제는 무엇입니까?",
                "이 문제를 겪는 주요 고객층은 누구입니까? 구체적으로 설명해주세요.",
                "당신의 제품/서비스가 제공하는 핵심 가치는 무엇입니까?",
                "고객에게 제품/서비스를 전달하기 위한 주요 채널은 무엇입니까?",
                "고객과 어떤 방식으로 관계를 형성하고 유지할 계획입니까?",
                "이 사업을 운영하기 위해 필요한 핵심 자원은 무엇입니까?",
                "사업 성공을 위해 반드시 수행해야 하는 핵심 활동은 무엇입니까?",
                "사업 성공을 위해 협력해야 할 핵심 파트너는 누구입니까?",
                "주요 비용 구조는 어떻게 구성될 예정입니까?",
                "어떤 방식으로 수익을 창출할 계획입니까?",
            )

        val answers = bmcQuestion.getAllAnswers()

        return buildString {
            appendLine("당신은 비즈니스 모델 캔버스(BMC) 전문가입니다.")
            appendLine("사용자가 제공한 사업 아이디어와 질문-답변을 바탕으로 BMC의 9가지 요소를 작성해주세요.")
            appendLine()
            appendLine("사업 아이디어: ${bmcQuestion.businessIdea}")
            appendLine()
            appendLine("질문과 답변:")
            questions.forEachIndexed { index, question ->
                val answer = answers[index]
                appendLine("${index + 1}. $question")
                appendLine("답변: ${answer ?: "답변 없음"}")
                appendLine()
            }
            appendLine()
            appendLine("위 정보를 바탕으로 다음 9가지 BMC 요소를 구체적이고 실용적으로 작성해주세요:")
            appendLine()
            appendLine("1. 핵심 파트너 (Key Partners): 사업 성공을 위한 핵심 파트너와 협력업체")
            appendLine("2. 핵심 활동 (Key Activities): 비즈니스 모델이 작동하기 위한 핵심 활동")
            appendLine("3. 핵심 자원 (Key Resources): 사업 운영에 필요한 핵심 자원")
            appendLine("4. 제공 가치 (Value Proposition): 고객에게 제공하는 핵심 가치")
            appendLine("5. 고객 관계 (Customer Relationships): 고객과의 관계 형성 및 유지 방식")
            appendLine("6. 채널 (Channels): 고객에게 가치를 전달하는 경로")
            appendLine("7. 목표 고객 (Customer Segments): 주요 고객층")
            appendLine("8. 비용 구조 (Cost Structure): 주요 비용 요소")
            appendLine("9. 수익 구조 (Revenue Streams): 수익 창출 방식")
            appendLine()
            appendLine("각 요소는 한국어로 작성하고, 구체적이고 실행 가능한 내용으로 작성해주세요.")
            appendLine("각 요소는 2-4개의 핵심 포인트로 구성하고, 불필요한 설명은 제외해주세요.")
            appendLine()
            appendLine("응답 형식:")
            appendLine("KEY_PARTNERS: (내용)")
            appendLine("KEY_ACTIVITIES: (내용)")
            appendLine("KEY_RESOURCES: (내용)")
            appendLine("VALUE_PROPOSITION: (내용)")
            appendLine("CUSTOMER_RELATIONSHIPS: (내용)")
            appendLine("CHANNELS: (내용)")
            appendLine("CUSTOMER_SEGMENTS: (내용)")
            appendLine("COST_STRUCTURE: (내용)")
            appendLine("REVENUE_STREAMS: (내용)")
        }
    }
}
