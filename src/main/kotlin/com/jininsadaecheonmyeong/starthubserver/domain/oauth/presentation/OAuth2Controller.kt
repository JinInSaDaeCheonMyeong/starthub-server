package com.jininsadaecheonmyeong.starthubserver.domain.oauth.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.oauth.docs.OAuth2Docs
import com.jininsadaecheonmyeong.starthubserver.domain.oauth.exception.InvalidStateException
import com.jininsadaecheonmyeong.starthubserver.domain.oauth.service.OAuth2Service
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthResponse
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
): OAuth2Docs {

    @GetMapping("/state")
    fun generateOAuthState(session: HttpSession): ResponseEntity<BaseResponse<String>> {
        val state = UUID.randomUUID().toString()
        session.setAttribute("state", state)
        return BaseResponse.of(state, "state 발급 완료")
    }

    @GetMapping("/google/web")
    override fun googleAuthWeb(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession
    ): ResponseEntity<BaseResponse<OAuthResponse>> {
        validateState(session, state)

        val response = oAuth2Service.googleAuthWeb(code)
        return BaseResponse.of(response, "구글 웹 로그인 성공")
    }

    @GetMapping("/google/app")
    override fun googleAuthApp(
        @RequestParam code: String,
        @RequestParam state: String,
        @RequestParam platform: String,
        @RequestParam codeVerifier: String,
        session: HttpSession
    ): ResponseEntity<BaseResponse<OAuthResponse>> {
        validateState(session, state)

        val response = oAuth2Service.googleAuthApp(code, platform, codeVerifier)
        return BaseResponse.of(response, "구글 앱 로그인 성공")
    }

    @GetMapping("/naver")
    override fun naverAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession
    ): ResponseEntity<BaseResponse<OAuthResponse>> {
        validateState(session, state)
        return BaseResponse.of(oAuth2Service.naverAuth(code), "네이버 로그인 성공")
    }

    @PostMapping("/apple")
    override fun appleAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession
    ): ResponseEntity<BaseResponse<OAuthResponse>> {
        validateState(session, state)
        return BaseResponse.of(oAuth2Service.appleAuth(code), "애플 로그인 성공")
    }

    private fun validateState(session: HttpSession, state: String?) {
        val sessionState = session.getAttribute("state") as? String
        if (state == null || sessionState == null || sessionState != state) {
            throw InvalidStateException("state 불일치")
        }
    }
}