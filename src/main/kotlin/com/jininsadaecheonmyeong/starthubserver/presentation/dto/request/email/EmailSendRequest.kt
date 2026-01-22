package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.email

import jakarta.validation.constraints.Email

data class EmailSendRequest(
    @field:Email val email: String,
)
