package com.jininsadaecheonmyeong.starthubserver.domain.email.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.email.data.EmailSendRequest
import com.jininsadaecheonmyeong.starthubserver.domain.email.data.EmailVerifyRequest
import com.jininsadaecheonmyeong.starthubserver.domain.email.docs.EmailDocs
import com.jininsadaecheonmyeong.starthubserver.domain.email.service.EmailVerificationService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/email")
class EmailController(
    private val emailVerificationService: EmailVerificationService,
) : EmailDocs {
    @PostMapping("/send-code")
    override fun sendVerificationCode(
        @RequestBody request: EmailSendRequest,
    ): ResponseEntity<BaseResponse<Unit>> {
        emailVerificationService.sendVerificationCode(request.email)
        return BaseResponse.of("전송 성공")
    }

    @PostMapping("/verify")
    override fun verifyEmail(
        @RequestBody request: EmailVerifyRequest,
    ): ResponseEntity<BaseResponse<Unit>> {
        emailVerificationService.verifyCode(request.email, request.code)
        return BaseResponse.of("코드 확인 성공")
    }
}
