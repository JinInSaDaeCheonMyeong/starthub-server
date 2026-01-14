package com.jininsadaecheonmyeong.starthubserver.dto.response.bmc

import com.jininsadaecheonmyeong.starthubserver.entity.bmc.BmcQuestion
import java.time.LocalDateTime

data class BmcSessionResponse(
    val id: Long,
    val sessionId: Long,
    val title: String,
    val isCompleted: Boolean,
    val createdAt: LocalDateTime,
    val questions: List<BmcQuestionResponse>,
) {
    companion object {
        fun from(bmcQuestion: BmcQuestion): BmcSessionResponse {
            return BmcSessionResponse(
                id = bmcQuestion.id!!,
                sessionId = bmcQuestion.id!!,
                title = bmcQuestion.title,
                isCompleted = bmcQuestion.isCompleted,
                createdAt = bmcQuestion.createdAt,
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
