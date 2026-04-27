package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document

import com.jininsadaecheonmyeong.starthubserver.domain.enums.document.DocumentType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateDocumentRequest(
    @field:NotBlank(message = "제목을 입력해주세요.")
    @field:Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하로 입력해주세요.")
    val title: String,
    val documentType: DocumentType,
)