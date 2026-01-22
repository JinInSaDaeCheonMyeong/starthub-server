package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.bmc

import com.jininsadaecheonmyeong.starthubserver.domain.enums.bmc.BmcTemplateType
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class UpdateBmcSessionRequest(
    @field:NotEmpty(message = "수정할 답변이 최소 1개 이상 필요합니다.")
    @field:Valid
    @ArraySchema(
        schema = Schema(implementation = AnswerUpdate::class),
        minItems = 1,
    )
    @Schema(
        example = """[
            {
                "questionNumber": 1,
                "answer": "수정된 답변 1"
            },
            {
                "questionNumber": 2,
                "answer": "수정된 답변 2"
            }
        ]""",
    )
    val answers: List<AnswerUpdate>,
    val templateType: BmcTemplateType? = null,
)

data class AnswerUpdate(
    @field:NotNull(message = "질문 번호는 필수입니다.")
    @field:Min(value = 1, message = "질문 번호는 1 이상이어야 합니다.")
    @field:Max(value = 10, message = "질문 번호는 10 이하여야 합니다.")
    var questionNumber: Int,
    @field:NotBlank(message = "답변은 필수입니다.")
    val answer: String,
)
