package com.jininsadaecheonmyeong.starthubserver.domain.email.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.email.data.EmailSendRequest
import com.jininsadaecheonmyeong.starthubserver.domain.email.data.EmailVerifyRequest
import com.jininsadaecheonmyeong.starthubserver.domain.email.service.EmailVerificationService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "이메일", description = "이메일 인증 및 검사 관련 API")
@RestController
@RequestMapping("/email")
class EmailController(
    private val emailVerificationService: EmailVerificationService
) {
    @Operation(summary = "인증 코드 발송")
    @PostMapping("/send-code")
    fun sendVerificationCode(@RequestBody request: EmailSendRequest) =
        BaseResponse.of(emailVerificationService.sendVerificationCode(request.email), "전송 성공")

    @Operation(summary = "인증 코드 확인")
    @PostMapping("/verify")
    fun verifyEmail(@RequestBody request: EmailVerifyRequest) =
        BaseResponse.of(emailVerificationService.verifyCode(request.email, request.code), "코드 확인 성공")
}