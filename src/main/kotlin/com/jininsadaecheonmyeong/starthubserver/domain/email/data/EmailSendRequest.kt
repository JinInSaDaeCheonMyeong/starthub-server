package com.jininsadaecheonmyeong.starthubserver.domain.email.data

import jakarta.validation.constraints.Email

data class EmailSendRequest (
    @field:Email val email: String
)