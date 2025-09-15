package com.jininsadaecheonmyeong.starthubserver.domain.bmc.service

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BmcQuestion
import org.springframework.stereotype.Service

@Service
class BmcPromptService {
    fun generateBmcPrompt(bmcQuestion: BmcQuestion): String {
        val questions =
            listOf(
                "사업 아이디어는 무엇인가요?\n" +
                    "제품이나 서비스를 간단하게 설명해주세요.",
                "우리의 핵심 고객은 누구인가요?\n" +
                    "우리가 해결해 주고자 하는 고객의 가장 큰 문제 또는 욕구는 무엇인가요?",
                "우리는 어떤 핵심 가치를 제공하나요?\n" +
                    "고객의 문제를 해결하기 위해 우리가 제공하는 핵심적인 가치는 무엇인가요?",
                "고객은 우리를 어떻게 만나고 경험하나요?\n" +
                    "어떤 경로를 통해 우리 서비스를 이용하고, 구매를 결정하게 되나요?",
                "고객과 어떻게 관계를 맺고 유지하나요?\n" +
                    "고객이 우리 서비스에 만족하고 계속 사용하게 하려면 어떤 관계를 맪어야 할까요?",
                "수익은 어떻게 창출되나요?\n" +
                    "구체적으로 어떤 방식으로 수익이 발생하게 되나요?",
                "우리의 핵심 자원은 무엇인가요?\n" +
                    "이 비즈니스를 운영하는 데 반드시 필요한 기술, 특허, 데이터, 인력, 자금 등은 무엇인가요?",
                "어떤 핵심 활동에 집중해야 하나요?\n" +
                    "제품 개발, 마케팅, 고객 관리 등 우리의 시간과 노력이 가장 많이 투입되어야 하는 일은 무엇인가요?",
                "누구와 협력해야 하나요?\n" +
                    "이 비즈니스를 성공시키기 위해 어떤 파트너와의 협력이 필요한가요?",
                "어떤 비용이 발생하나요?\n" +
                    "서비스를 개발하고 고객에게 가치를 제공하는 과정에서 발생하는 가장 큰 비용은 무엇인가요?",
            )

        val answers = bmcQuestion.getAllAnswers()

        return buildString {
            appendLine("당신은 비즈니스 모델 캔버스(BMC) 전문가입니다.")
            appendLine("사용자가 제공한 사업 아이디어와 질문-답변을 바탕으로 BMC의 9가지 요소를 작성해주세요.")
            appendLine()
            appendLine("사업 제목: ${bmcQuestion.title}")
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
            appendLine("1. 목표 고객 (Customer Segments): 주요 고객층")
            appendLine("2. 제공 가치 (Value Proposition): 고객에게 제공하는 핵심 가치")
            appendLine("3. 채널 (Channels): 고객에게 가치를 전달하는 경로")
            appendLine("4. 고객 관계 (Customer Relationships): 고객과의 관계 형성 및 유지 방식")
            appendLine("5. 수익 구조 (Revenue Streams): 수익 창출 방식")
            appendLine("6. 핵심 자원 (Key Resources): 사업 운영에 필요한 핵심 자원")
            appendLine("7. 핵심 활동 (Key Activities): 비즈니스 모델이 작동하기 위한 핵심 활동")
            appendLine("8. 핵심 파트너 (Key Partners): 사업 성공을 위한 핵심 파트너와 협력업체")
            appendLine("9. 비용 구조 (Cost Structure): 주요 비용 요소")
            appendLine()
            appendLine("각 요소는 한국어로 작성하고, 구체적이고 실행 가능한 내용으로 작성해주세요.")
            appendLine("각 요소는 2-4개의 핵심 포인트로 구성하고, 불필요한 설명은 제외해주세요.")
            appendLine()
            appendLine("응답 형식:")
            appendLine("CUSTOMER_SEGMENTS: (내용)")
            appendLine("VALUE_PROPOSITION: (내용)")
            appendLine("CHANNELS: (내용)")
            appendLine("CUSTOMER_RELATIONSHIPS: (내용)")
            appendLine("REVENUE_STREAMS: (내용)")
            appendLine("KEY_RESOURCES: (내용)")
            appendLine("KEY_ACTIVITIES: (내용)")
            appendLine("KEY_PARTNERS: (내용)")
            appendLine("COST_STRUCTURE: (내용)")
        }
    }
}
