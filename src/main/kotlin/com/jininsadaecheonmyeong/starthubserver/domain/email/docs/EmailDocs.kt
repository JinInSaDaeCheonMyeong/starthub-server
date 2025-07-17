package com.jininsadaecheonmyeong.starthubserver.domain.email.docs

import com.jininsadaecheonmyeong.starthubserver.domain.email.data.EmailSendRequest
import com.jininsadaecheonmyeong.starthubserver.domain.email.data.EmailVerifyRequest
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "이메일", description = "이메일 인증 및 검사 관련 API")
interface EmailDocs {
    @Operation(summary = "인증 코드 발송")
    fun sendVerificationCode(
        @RequestBody request: EmailSendRequest,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(summary = "인증 코드 확인")
    fun verifyEmail(
        @RequestBody request: EmailVerifyRequest,
    ): ResponseEntity<BaseResponse<Unit>>
}
