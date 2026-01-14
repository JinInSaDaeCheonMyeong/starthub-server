package com.jininsadaecheonmyeong.starthubserver.controller.oauth

import com.jininsadaecheonmyeong.starthubserver.docs.oauth.OAuthDocs
import com.jininsadaecheonmyeong.starthubserver.dto.request.oauth.AppleAppLoginRequest
import com.jininsadaecheonmyeong.starthubserver.exception.oauth.InvalidStateException
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthResponse
import com.jininsadaecheonmyeong.starthubserver.usecase.oauth.OAuthUseCase
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/oauth")
class OAuthController(
    private val oAuthUseCase: OAuthUseCase,
) : OAuthDocs {
    @GetMapping("/state")
    override fun generateOAuthState(session: HttpSession): ResponseEntity<BaseResponse<String>> {
        val state = UUID.randomUUID().toString()
        session.setAttribute("state", state)
        return BaseResponse.of(state, "state 발급 완료")
    }

    @GetMapping("/google/app")
    override fun googleAuthApp(
        @RequestParam code: String,
        @RequestParam state: String,
        @RequestParam platform: String,
        @RequestParam codeVerifier: String,
        session: HttpSession,
    ): ResponseEntity<BaseResponse<OAuthResponse>> {
        validateState(session, state)

        val response = oAuthUseCase.googleAuthApp(code, platform, codeVerifier)
        return BaseResponse.of(response, "구글 앱 로그인 성공")
    }

    @GetMapping("/naver/app")
    override fun naverAuthApp(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession,
    ): ResponseEntity<BaseResponse<OAuthResponse>> {
        validateState(session, state)

        val response = oAuthUseCase.naverAuthApp(code)
        return BaseResponse.of(response, "네이버 앱 로그인 성공")
    }

    @PostMapping("/apple/app")
    override fun appleAuthApp(
        @RequestBody request: AppleAppLoginRequest,
    ): ResponseEntity<BaseResponse<OAuthResponse>> {
        val response = oAuthUseCase.appleAuthApp(request.idToken)
        return BaseResponse.of(response, "애플 앱 로그인 성공")
    }

    private fun validateState(
        session: HttpSession,
        state: String?,
    ) {
        val sessionState = session.getAttribute("state") as? String
        if (state == null || sessionState == null || sessionState != state) {
            throw InvalidStateException("state 불일치")
        }
    }
}
