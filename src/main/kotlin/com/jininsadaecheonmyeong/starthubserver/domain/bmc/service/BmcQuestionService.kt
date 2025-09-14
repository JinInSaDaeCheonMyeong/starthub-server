package com.jininsadaecheonmyeong.starthubserver.domain.bmc.service

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.AnswerQuestionRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.CreateBmcSessionRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BmcFormResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BmcSessionResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BmcQuestion
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.exception.BmcSessionNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository.BmcQuestionRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BmcQuestionService(
    private val bmcQuestionRepository: BmcQuestionRepository,
) {
    fun createBmcSession(request: CreateBmcSessionRequest): BmcSessionResponse {
        val user = UserAuthenticationHolder.current()

        val bmcQuestion =
            BmcQuestion(
                user = user,
                title = request.title,
            )

        val savedBmcQuestion = bmcQuestionRepository.save(bmcQuestion)
        return BmcSessionResponse.from(savedBmcQuestion)
    }

    fun answerQuestion(request: AnswerQuestionRequest): BmcSessionResponse {
        val user = UserAuthenticationHolder.current()
        val bmcQuestion =
            bmcQuestionRepository.findByIdAndUser(request.sessionId, user)
                .orElseThrow { BmcSessionNotFoundException("BMC 세션을 찾을 수 없습니다.") }

        bmcQuestion.updateAnswer(request.questionNumber, request.answer)
        if (isSessionCompleted(bmcQuestion)) bmcQuestion.markAsCompleted()
        val savedBmcQuestion = bmcQuestionRepository.save(bmcQuestion)
        return BmcSessionResponse.from(savedBmcQuestion)
    }

    @Transactional(readOnly = true)
    fun getBmcSession(sessionId: Long): BmcSessionResponse {
        val user = UserAuthenticationHolder.current()
        val bmcQuestion =
            bmcQuestionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow { BmcSessionNotFoundException("BMC 세션을 찾을 수 없습니다.") }

        return BmcSessionResponse.from(bmcQuestion)
    }

    @Transactional(readOnly = true)
    fun getAllBmcSessions(): List<BmcSessionResponse> {
        val user = UserAuthenticationHolder.current()
        val bmcQuestions = bmcQuestionRepository.findAllByUserOrderByCreatedAtDesc(user)

        return bmcQuestions.map { BmcSessionResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getBmcQuestions(): List<BmcFormResponse> {
        val questions =
            listOf(
                "사업 아이디어는 무엇인가요?\n" +
                    "어떤 문제를 해결하거나 어떤 가치를 제공하는 사업인지 구체적으로 설명해주세요.\n" +
                    "이 아이디어가 어떻게 시장에서 차별화될 수 있을지도 함께 말씀해주세요.",
                "핵심 고객은 누구인가요? (Customer Segments + Problem)\n" +
                    "우리 서비스/제품을 꼭 필요로 하는 '핵심 고객'은 누구이며, 그들이 해결하고 싶어 하는 가장 큰 문제·욕구는 무엇인가요?\n" +
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

        return questions.mapIndexed { i, q ->
            BmcFormResponse(
                questionNumber = i.inc(),
                question = q,
            )
        }
    }

    private fun isSessionCompleted(bmcQuestion: BmcQuestion): Boolean {
        return bmcQuestion.getAllAnswers().all { it != null && it.isNotBlank() }
    }

    fun getBmcQuestionEntity(sessionId: Long): BmcQuestion {
        val user = UserAuthenticationHolder.current()
        return bmcQuestionRepository.findByIdAndUser(sessionId, user)
            .orElseThrow { BmcSessionNotFoundException("BMC 세션을 찾을 수 없습니다.") }
    }
}
