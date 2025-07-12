package com.jininsadaecheonmyeong.starthubserver.domain.bmc.service

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.AnswerQuestionRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.CreateBmcSessionRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BmcSessionResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BmcQuestion
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.exception.BmcSessionNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository.BmcQuestionRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class BmcQuestionService(
    private val bmcQuestionRepository: BmcQuestionRepository,
) {
    fun createBmcSession(request: CreateBmcSessionRequest): BmcSessionResponse {
        val user = UserAuthenticationHolder.current()
        val sessionId = UUID.randomUUID().toString()

        val bmcQuestion =
            BmcQuestion(
                user = user,
                sessionId = sessionId,
                businessIdea = request.businessIdea,
            )

        val savedBmcQuestion = bmcQuestionRepository.save(bmcQuestion)
        return BmcSessionResponse.from(savedBmcQuestion)
    }

    fun answerQuestion(request: AnswerQuestionRequest): BmcSessionResponse {
        val user = UserAuthenticationHolder.current()
        val bmcQuestion =
            bmcQuestionRepository.findBySessionIdAndUser(request.sessionId, user)
                .orElseThrow { BmcSessionNotFoundException("BMC 세션을 찾을 수 없습니다.") }

        bmcQuestion.updateAnswer(request.questionNumber, request.answer)

        if (isSessionCompleted(bmcQuestion)) {
            bmcQuestion.markAsCompleted()
        }

        val savedBmcQuestion = bmcQuestionRepository.save(bmcQuestion)
        return BmcSessionResponse.from(savedBmcQuestion)
    }

    @Transactional(readOnly = true)
    fun getBmcSession(sessionId: String): BmcSessionResponse {
        val user = UserAuthenticationHolder.current()
        val bmcQuestion =
            bmcQuestionRepository.findBySessionIdAndUser(sessionId, user)
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
    fun getBmcQuestions(): List<String> {
        return listOf(
            "당신의 사업 아이디어로 해결하고자 하는 핵심 문제는 무엇입니까?",
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
    }

    private fun isSessionCompleted(bmcQuestion: BmcQuestion): Boolean {
        return bmcQuestion.getAllAnswers().all { it != null && it.isNotBlank() }
    }

    fun getBmcQuestionEntity(sessionId: String): BmcQuestion {
        val user = UserAuthenticationHolder.current()
        return bmcQuestionRepository.findBySessionIdAndUser(sessionId, user)
            .orElseThrow { BmcSessionNotFoundException("BMC 세션을 찾을 수 없습니다.") }
    }
}
