package com.jininsadaecheonmyeong.starthubserver.domain.oauth.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.oauth.service.OAuth2Service
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthProvider
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "OAuth", description = "소셜 로그인 관련 API")
@RestController
@RequestMapping("/oauth")
class OAuth2Controller(
    private val oAuth2Service: OAuth2Service,
) {
    @GetMapping("/state")
    fun generateOAuthState(session: HttpSession): ResponseEntity<BaseResponse<String>> {
        val state = UUID.randomUUID().toString()
        session.setAttribute("oauth_state", state)
        return BaseResponse.ok(state, "OAuth state 발급 완료")
    }

    @GetMapping("/google")
    fun googleCallback(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession
    ): ResponseEntity<BaseResponse<TokenResponse>> {
        val sessionState = session.getAttribute("oauth_state") as? String
        if (sessionState == null || sessionState != state) {
            throw IllegalStateException("State mismatch - CSRF 가능성")
        }
        return BaseResponse.ok(oAuth2Service.googleAuth(code, AuthProvider.GOOGLE), "구글 로그인 성공")
    }

    @GetMapping("/naver")
    fun naverCallback(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession
    ): ResponseEntity<BaseResponse<TokenResponse>> {
        val sessionState = session.getAttribute("oauth_state") as? String
        if (sessionState == null || sessionState != state) {
            throw IllegalStateException("State mismatch - CSRF 가능성")
        }
        return BaseResponse.ok(oAuth2Service.naverAuth(code, AuthProvider.NAVER), "네이버 로그인 성공")
    }

    @PostMapping("/apple")
    fun appleCallback(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession
    ): ResponseEntity<BaseResponse<TokenResponse>> {
        val sessionState = session.getAttribute("oauth_state") as? String
        if (sessionState == null || sessionState != state) {
            throw IllegalStateException("State mismatch - CSRF 가능성")
        }
        return BaseResponse.ok(oAuth2Service.appleAuth(code, AuthProvider.APPLE), "애플 로그인 성공")
    }
}