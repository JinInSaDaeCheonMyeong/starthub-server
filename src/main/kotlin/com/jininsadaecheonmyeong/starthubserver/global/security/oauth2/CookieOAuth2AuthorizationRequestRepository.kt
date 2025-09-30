package com.jininsadaecheonmyeong.starthubserver.global.security.oauth2

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import org.springframework.util.SerializationUtils
import java.util.Base64

@Component
class CookieOAuth2AuthorizationRequestRepository(
) : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    private val cookieName = "oauth2_auth_request"
    private val cookieExpireSeconds = 180

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val cookie = request.cookies?.find { it.name == cookieName }
        return cookie?.let { deserialize(it.value) }
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response)
            return
        }

        val cookieValue = serialize(authorizationRequest)
        val cookie =
            jakarta.servlet.http.Cookie(cookieName, cookieValue).apply {
                path = "/"
                isHttpOnly = true
                maxAge = cookieExpireSeconds
            }
        response.addCookie(cookie)
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): OAuth2AuthorizationRequest? {
        val authorizationRequest = loadAuthorizationRequest(request)
        removeAuthorizationRequestCookies(request, response)
        return authorizationRequest
    }

    private fun removeAuthorizationRequestCookies(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        request.cookies?.filter { it.name == cookieName }?.forEach {
            it.value = ""
            it.path = "/"
            it.maxAge = 0
            response.addCookie(it)
        }
    }

    private fun serialize(obj: Any): String {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(obj))
    }

    private fun deserialize(serialized: String): OAuth2AuthorizationRequest? {
        return try {
            val decoded = Base64.getUrlDecoder().decode(serialized)
            SerializationUtils.deserialize(decoded) as? OAuth2AuthorizationRequest
        } catch (_: Exception) {
            null
        }
    }
}
