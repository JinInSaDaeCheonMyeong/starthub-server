package com.jininsadaecheonmyeong.starthubserver.domain.oauth2.handler

import com.jininsadaecheonmyeong.starthubserver.domain.oauth2.data.CustomOAuth2User
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2SuccessHandler(
    private val tokenProvider: TokenProvider,
) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val customOAuth2User = authentication.principal as CustomOAuth2User
        val user = customOAuth2User.user

        val accessToken = tokenProvider.generateAccess(user)
        val refreshToken = tokenProvider.generateRefresh(user)

        val targetUrl = 
            UriComponentsBuilder.fromUriString("http://localhost:3000/oauth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("isFirstLogin", customOAuth2User.isFirstLogin)
                .build().toUriString()

        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
