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
    private val userAuthenticationHolder: UserAuthenticationHolder
) {
    fun createBmcSession(request: CreateBmcSessionRequest): BmcSessionResponse {
        val user = userAuthenticationHolder.current()

        val bmcQuestion =
            BmcQuestion(
                user = user,
                title = request.title,
            )

        val savedBmcQuestion = bmcQuestionRepository.save(bmcQuestion)
        return BmcSessionResponse.from(savedBmcQuestion)
    }

    fun answerQuestion(request: AnswerQuestionRequest): BmcSessionResponse {
        val user = userAuthenticationHolder.current()
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
        val user = userAuthenticationHolder.current()
        val bmcQuestion =
            bmcQuestionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow { BmcSessionNotFoundException("BMC 세션을 찾을 수 없습니다.") }

        return BmcSessionResponse.from(bmcQuestion)
    }

    @Transactional(readOnly = true)
    fun getAllBmcSessions(): List<BmcSessionResponse> {
        val user = userAuthenticationHolder.current()
        val bmcQuestions = bmcQuestionRepository.findAllByUserOrderByCreatedAtDesc(user)

        return bmcQuestions.map { BmcSessionResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getBmcQuestions(): List<BmcFormResponse> {
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
                    "고객이 우리 서비스에 만족하고 계속 사용하게 하려면 어떤 관계를 맺어야 할까요?",
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
        val user = userAuthenticationHolder.current()
        return bmcQuestionRepository.findByIdAndUser(sessionId, user)
            .orElseThrow { BmcSessionNotFoundException("BMC 세션을 찾을 수 없습니다.") }
    }
}
