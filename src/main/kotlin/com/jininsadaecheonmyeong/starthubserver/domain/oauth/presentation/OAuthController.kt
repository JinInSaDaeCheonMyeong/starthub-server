package com.jininsadaecheonmyeong.starthubserver.domain.oauth.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.oauth.docs.OAuthDocs
import com.jininsadaecheonmyeong.starthubserver.domain.oauth.exception.InvalidStateException
import com.jininsadaecheonmyeong.starthubserver.domain.oauth.service.OAuthService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthProperties
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthResponse
import com.jininsadaecheonmyeong.starthubserver.global.security.token.properties.TokenProperties
import com.jininsadaecheonmyeong.starthubserver.global.support.CookieUtil
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView
import java.util.*

@Tag(name = "OAuth", description = "소셜 로그인 관련 API")
@RestController
@RequestMapping("/oauth")
class OAuthController(
    private val oAuthService: OAuthService,
    private val oAuthProperties: OAuthProperties,
    private val cookieUtil: CookieUtil,
    private val tokenProperties: TokenProperties
): OAuthDocs {

    @GetMapping("/state")
    override fun generateOAuthState(session: HttpSession): ResponseEntity<BaseResponse<String>> {
        val state = UUID.randomUUID().toString()
        session.setAttribute("state", state)
        return BaseResponse.of(state, "state 발급 완료")
    }

    @GetMapping("/google/web")
    override fun googleAuthWeb(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession,
        response: HttpServletResponse
    ): RedirectView {
        validateState(session, state)

        val oAuthResponse = oAuthService.googleAuthWeb(code)
        addRefreshTokenToCookie(response, oAuthResponse.refresh)

        return RedirectView(
            "${oAuthProperties.frontRedirectUri}?access=${oAuthResponse.access}&isFirstLogin=${oAuthResponse.isFirstLogin}"
        )
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

        val response = oAuthService.googleAuthApp(code, platform, codeVerifier)
        return BaseResponse.of(response, "구글 앱 로그인 성공")
    }

    @GetMapping("/naver")
    override fun naverAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession,
        response: HttpServletResponse
    ): RedirectView {
        validateState(session, state)
        val oAuthResponse = oAuthService.naverAuth(code)
        addRefreshTokenToCookie(response, oAuthResponse.refresh)

        return RedirectView(
            "${oAuthProperties.frontRedirectUri}?access=${oAuthResponse.access}&isFirstLogin=${oAuthResponse.isFirstLogin}"
        )
    }

    @PostMapping("/apple")
    override fun appleAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession,
        response: HttpServletResponse
    ): RedirectView {
        validateState(session, state)
        val oAuthResponse = oAuthService.appleAuth(code)
        addRefreshTokenToCookie(response, oAuthResponse.refresh)

        return RedirectView(
            "${oAuthProperties.frontRedirectUri}?access=${oAuthResponse.access}&isFirstLogin=${oAuthResponse.isFirstLogin}"
        )
    }

    private fun addRefreshTokenToCookie(response: HttpServletResponse, refreshToken: String) {
        cookieUtil.addCookie(response, "refresh", refreshToken, tokenProperties.refresh, true)
    }

    private fun validateState(session: HttpSession, state: String?) {
        val sessionState = session.getAttribute("state") as? String
        if (state == null || sessionState == null || sessionState != state) {
            throw InvalidStateException("state 불일치")
        }
    }
}