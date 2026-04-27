package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document

data class UpdateDocumentRequest(
    val title: String? = null,
    val content: String? = null,
)