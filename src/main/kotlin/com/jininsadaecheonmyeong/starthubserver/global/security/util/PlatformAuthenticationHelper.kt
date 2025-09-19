package com.jininsadaecheonmyeong.starthubserver.global.security.util

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component

@Component
class PlatformAuthenticationHelper {
    fun isWebPlatform(request: HttpServletRequest): Boolean {
        return request.getHeader(PLATFORM_HEADER) == WEB_PLATFORM
    }

    fun isAppPlatform(request: HttpServletRequest): Boolean {
        return request.getHeader(PLATFORM_HEADER) == APP_PLATFORM
    }

    fun setTokenCookies(
        response: HttpServletResponse,
        accessToken: String,
        refreshToken: String,
    ) {
        setTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME, accessToken)
        setTokenCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken)
    }

    private fun setTokenCookie(
        response: HttpServletResponse,
        cookieName: String,
        tokenValue: String,
    ) {
        val cookie =
            ResponseCookie.from(cookieName, tokenValue)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(COOKIE_MAX_AGE.toLong())
                .path(COOKIE_PATH)
                .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }

    fun clearTokenCookies(response: HttpServletResponse) {
        clearTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME)
        clearTokenCookie(response, REFRESH_TOKEN_COOKIE_NAME)
    }

    private fun clearTokenCookie(
        response: HttpServletResponse,
        cookieName: String,
    ) {
        val cookie =
            ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(0)
                .path(COOKIE_PATH)
                .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }

    companion object {
        private const val PLATFORM_HEADER = "X-Platform"
        private const val WEB_PLATFORM = "web"
        private const val APP_PLATFORM = "app"
        private const val ACCESS_TOKEN_COOKIE_NAME = "access_token"
        private const val REFRESH_TOKEN_COOKIE_NAME = "refresh_token"
        private const val COOKIE_MAX_AGE = 7 * 24 * 60 * 60 // 7일
        private const val COOKIE_PATH = "/"
    }
}
