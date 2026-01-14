package com.jininsadaecheonmyeong.starthubserver.controller.email

import com.jininsadaecheonmyeong.starthubserver.docs.email.EmailDocs
import com.jininsadaecheonmyeong.starthubserver.dto.request.email.EmailSendRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.email.EmailVerifyRequest
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.usecase.email.EmailUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/email")
class EmailController(
    private val emailUseCase: EmailUseCase,
) : EmailDocs {
    @PostMapping("/send-code")
    override fun sendVerificationCode(
        @Valid @RequestBody request: EmailSendRequest,
    ): ResponseEntity<BaseResponse<Unit>> {
        emailUseCase.sendVerificationCode(request.email)
        return BaseResponse.of("전송 성공")
    }

    @PostMapping("/verify")
    override fun verifyEmail(
        @Valid @RequestBody request: EmailVerifyRequest,
    ): ResponseEntity<BaseResponse<Unit>> {
        emailUseCase.verifyCode(request.email, request.code)
        return BaseResponse.of("코드 확인 성공")
    }
}
