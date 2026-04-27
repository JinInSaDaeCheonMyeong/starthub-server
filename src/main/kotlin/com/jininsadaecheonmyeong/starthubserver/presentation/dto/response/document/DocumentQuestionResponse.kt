package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document

import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.DocumentQuestion

data class DocumentQuestionResponse(
    val id: Long,
    val questionText: String,
    val answerText: String?,
    val orderIndex: Int,
    val required: Boolean,
) {
    companion object {
        fun from(question: DocumentQuestion) =
            DocumentQuestionResponse(
                id = question.id!!,
                questionText = question.questionText,
                answerText = question.answerText,
                orderIndex = question.orderIndex,
                required = question.required,
            )
    }
}
