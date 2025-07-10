package com.jininsadaecheonmyeong.starthubserver.global.support

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component

@Component
class CookieUtil {
    fun createCookie(name: String, value: String, maxAge: Long, httpOnly: Boolean): ResponseCookie =
        ResponseCookie.from(name, value)
            .path("/")
            .maxAge(maxAge / 1000)
            .httpOnly(httpOnly)
            .secure(true)
            .sameSite("None")
            .build()

    fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Long, httpOnly: Boolean) {
        val cookie = createCookie(name, value, maxAge, httpOnly)
        response.addHeader("Set-Cookie", cookie.toString())
    }
}