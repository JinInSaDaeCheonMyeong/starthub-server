package com.jininsadaecheonmyeong.starthubserver.infrastructure.handler.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2FailureHandler : SimpleUrlAuthenticationFailureHandler() {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        val targetUrl =
            UriComponentsBuilder.fromUriString("https://start-hub.kr/oauth/fail")
                .queryParam("error", exception.localizedMessage)
                .build().toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
