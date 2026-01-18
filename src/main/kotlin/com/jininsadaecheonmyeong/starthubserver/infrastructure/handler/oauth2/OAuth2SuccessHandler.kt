package com.jininsadaecheonmyeong.starthubserver.infrastructure.handler.oauth2

import com.jininsadaecheonmyeong.starthubserver.global.security.token.service.TokenService
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.oauth2.CustomOAuth2User
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2SuccessHandler(
    private val tokenService: TokenService,
) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val customOAuth2User = authentication.principal as CustomOAuth2User
        val user = customOAuth2User.user

        val accessToken = tokenService.generateAccess(user)
        val refreshToken = tokenService.generateAndStoreRefreshToken(user)

        val targetUrl =
            UriComponentsBuilder.fromUriString("https://start-hub.kr/oauth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("isFirstLogin", customOAuth2User.isFirstLogin)
                .build().toUriString()

        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
