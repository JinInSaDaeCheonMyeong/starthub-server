package com.jininsadaecheonmyeong.starthubserver.domain.oauth.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.oauth.docs.Oauth2Docs
import com.jininsadaecheonmyeong.starthubserver.domain.oauth.service.OAuth2Service
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.TokenResponse
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
): Oauth2Docs {

    @GetMapping("/state")
    fun generateOAuthState(session: HttpSession): ResponseEntity<BaseResponse<String>> {
        val state = UUID.randomUUID().toString()
        session.setAttribute("state", state)
        return BaseResponse.ok(state, "state 발급 완료")
    }

    @PostMapping("/google")
    override fun googleAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession
    ): ResponseEntity<BaseResponse<TokenResponse>> {
        validateState(session, state)
        return BaseResponse.ok(oAuth2Service.googleAuth(code), "구글 로그인 성공")
    }

    @PostMapping("/naver")
    override fun naverAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession
    ): ResponseEntity<BaseResponse<TokenResponse>> {
        validateState(session, state)
        return BaseResponse.ok(oAuth2Service.naverAuth(code), "네이버 로그인 성공")
    }

    @PostMapping("/apple")
    override fun appleAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession
    ): ResponseEntity<BaseResponse<TokenResponse>> {
        validateState(session, state)
        return BaseResponse.ok(oAuth2Service.appleAuth(code), "애플 로그인 성공")
    }

    private fun validateState(session: HttpSession, state: String?) {
        val sessionState = session.getAttribute("state") as? String
        if (state != null && (sessionState == null || sessionState != state)) {
            throw IllegalStateException("state 불일치")
        }
    }
}