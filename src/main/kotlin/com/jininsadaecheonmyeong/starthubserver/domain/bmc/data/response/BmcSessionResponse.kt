package com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BmcQuestion
import java.time.LocalDateTime
import java.util.UUID

data class BmcSessionResponse(
    val id: UUID,
    val sessionId: String,
    val businessIdea: String,
    val isCompleted: Boolean,
    val createdAt: LocalDateTime,
    val questions: List<BmcQuestionResponse>,
) {
    companion object {
        fun from(bmcQuestion: BmcQuestion): BmcSessionResponse {
            return BmcSessionResponse(
                id = bmcQuestion.id!!,
                sessionId = bmcQuestion.sessionId,
                businessIdea = bmcQuestion.businessIdea,
                isCompleted = bmcQuestion.isCompleted,
                createdAt = bmcQuestion.createdAt!!,
                questions =
                    (1..10).map { questionNumber ->
                        BmcQuestionResponse(
                            questionNumber = questionNumber,
                            answer = bmcQuestion.getAnswer(questionNumber),
                        )
                    },
            )
        }
    }
}

data class BmcQuestionResponse(
    val questionNumber: Int,
    val answer: String?,
)
