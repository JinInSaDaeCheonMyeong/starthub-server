package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document

import com.jininsadaecheonmyeong.starthubserver.domain.enums.document.ToneType

data class AnswerQuestionsRequest(
    val answers: List<QuestionAnswer>,
    val toneType: ToneType,
)

data class QuestionAnswer(
    val questionId: Long,
    val answer: String,
)