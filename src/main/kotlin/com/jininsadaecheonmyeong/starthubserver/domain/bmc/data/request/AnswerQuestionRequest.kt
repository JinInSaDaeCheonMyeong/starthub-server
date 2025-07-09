package com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AnswerQuestionRequest(
    @field:NotBlank(message = "세션 ID를 입력해주세요.")
    val sessionId: String,

    @field:Min(value = 1, message = "질문 번호는 1 이상이어야 합니다.")
    @field:Max(value = 10, message = "질문 번호는 10 이하여야 합니다.")
    val questionNumber: Int,

    @field:NotBlank(message = "답변을 입력해주세요.")
    @field:Size(min = 5, max = 1000, message = "답변은 5자 이상 1000자 이하로 입력해주세요.")
    val answer: String
)