package com.jininsadaecheonmyeong.starthubserver.domain.oauth2.handler

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
            UriComponentsBuilder.fromUriString("http://localhost:3000/oauth/fail")
                .queryParam("error", exception.localizedMessage)
                .build().toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
