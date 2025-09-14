package com.jininsadaecheonmyeong.starthubserver.domain.bmc.service

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BmcQuestion
import org.springframework.stereotype.Service

@Service
class BmcPromptService {
    fun generateBmcPrompt(bmcQuestion: BmcQuestion): String {
        val questions =
            listOf(
                "핵심 고객은 누구인가요? (Customer Segments + Problem)\n" +
                    "우리 서비스/제품을 꼭 필요로 하는 ‘핵심 고객’은 누구이며, 그들이 해결하고 싶어 하는 가장 큰 문제·욕구는 무엇인가요?\n" +
                    "이들은 어떤 상황에서 어떤 불편을 가장 자주 겪고 있을까요?",
                "고객에게 제공할 주요 가치와 해결책은 무엇인가요? (Value Proposition)\n" +
                    "이 문제를 해결하기 위해 우리가 제공하는 '핵심 가치'는 무엇인가요?\n" +
                    "어떤 방식으로 문제를 해결하며, 우리가 그걸 할 수 있는 이유(강점/자원)는 무엇인가요?\n",
                "고객은 어떤 경로를 통해 우리 제품을 만나고 사용하는가요? (Channels)\n" +
                    "고객이 우리 제품/서비스를 어떻게 발견하고, 어떤 경로로 이용하며, 어떻게 다시 찾아오게 되나요?\n" +
                    "이 흐름의 각 단계에서 우리가 어떤 접점을 제공해야 할까요?",
                "고객과 어떤 관계를 유지할 계획인가요? (Customer Relationships)\n" +
                    "고객이 우리 서비스에 만족하고 지속적으로 사용할 수 있도록, 어떤 방식으로 관계를 형성하고 유지할 계획인가요?\n" +
                    "개인화, 소통, 피드백 등은 어떻게 제공되나요?",
                "고객은 언제, 무엇에 대해 비용을 지불하나요? (Revenue Streams)\n" +
                    "고객은 어떤 시점에 어떤 방식으로 우리에게 비용을 지불하나요?\n" +
                    "우리는 어떤 수익 구조를 기대할 수 있을까요?\n",
                "꼭 필요한 핵심 자원은 무엇인가요? (Key Resources)\n" +
                    "이 비즈니스를 운영하는 데 꼭 필요한 기술, 인력, 시스템, 데이터 등은 무엇인가요?\n" +
                    "우리가 가진 가장 중요한 자산은 무엇인가요?\n",
                "이 가치를 만들기 위한 핵심 활동은 무엇인가요? (Key Activities)\n" +
                    "우리의 제품/서비스를 만들고 유지하기 위해 반드시 수행해야 하는 핵심 업무는 무엇인가요?\n" +
                    "무엇에 가장 많은 시간과 노력이 들어가나요?\n",
                "함께해야 할 핵심 파트너는 누구인가요? (Key Partnership)\n" +
                    "우리가 혼자 하기 어려운 부분을 맡아줄 파트너는 누구이며, 어떤 역할을 하게 되나요?\n" +
                    "플랫폼, API 공급사, 콘텐츠 제공자 등 어떤 협력이 필요한가요?",
                "이 서비스를 운영하는 데 어떤 비용이 드나요? (Cost Structure)\n" +
                    "이 가치를 고객에게 제공하기 위해 가장 많이 드는 비용 항목은 무엇인가요?\n" +
                    "정기적으로 드는 고정비와, 상황에 따라 변동되는 변동비는 어떤 것들이 있나요? \n",
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
