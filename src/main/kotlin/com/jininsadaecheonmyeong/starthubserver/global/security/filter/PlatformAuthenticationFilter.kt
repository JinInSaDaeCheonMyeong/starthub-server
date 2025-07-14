package com.jininsadaecheonmyeong.starthubserver.global.security.filter

import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidTokenException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenParser
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenValidator
import com.jininsadaecheonmyeong.starthubserver.global.security.token.enums.TokenType
import com.jininsadaecheonmyeong.starthubserver.global.security.token.exception.ExpiredTokenException
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.CustomUserDetails
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class PlatformAuthenticationFilter(
    private val tokenValidator: TokenValidator,
    private val tokenParser: TokenParser,
    private val userRepository: UserRepository,
) : OncePerRequestFilter() {
    companion object {
        private const val TOKEN_SECURE_TYPE = "Bearer "
        private const val ACCESS_TOKEN_COOKIE_NAME = "access_token"
        private const val PLATFORM_HEADER = "X-Platform"
        private const val WEB_PLATFORM = "web"
        private const val APP_PLATFORM = "app"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val platform = request.getHeader(PLATFORM_HEADER)
        var token: String? = null

        when (platform) {
            WEB_PLATFORM -> {
                token = extractTokenFromCookie(request)
            }
            APP_PLATFORM -> {
                token = extractTokenFromHeader(request)
            }
            else -> {
                token = extractTokenFromHeader(request) ?: extractTokenFromCookie(request)
            }
        }

        if (token != null) {
            try {
                tokenValidator.validateAll(token, TokenType.ACCESS_TOKEN)
                setAuthentication(token)
            } catch (e: Exception) {
                when {
                    isTokenExpiredException(e) -> throw ExpiredTokenException("토큰이 만료되었습니다.")
                    else -> throw InvalidTokenException("유효하지 않은 토큰입니다.")
                }
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromHeader(request: HttpServletRequest): String? {
        val authorizationHeader = request.getHeader("Authorization")

        return if (!authorizationHeader.isNullOrEmpty() && authorizationHeader.startsWith(TOKEN_SECURE_TYPE)) {
            authorizationHeader.removePrefix(TOKEN_SECURE_TYPE)
        } else {
            null
        }
    }

    private fun extractTokenFromCookie(request: HttpServletRequest): String? {
        return request.cookies?.find { it.name == ACCESS_TOKEN_COOKIE_NAME }?.value
    }

    private fun setAuthentication(token: String) {
        val user = getUserDetails(token)
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(user, null, user.authorities)
    }

    private fun getUserDetails(token: String): CustomUserDetails {
        return CustomUserDetails(
            userRepository.findByEmail(tokenParser.findEmail(token))
                ?: throw RuntimeException("찾을 수 없는 유저"),
        )
    }

    private fun isTokenExpiredException(e: Exception): Boolean {
        return e.message?.contains("expired", ignoreCase = true) == true ||
            e.message?.contains("만료", ignoreCase = true) == true ||
            e is io.jsonwebtoken.ExpiredJwtException
    }
}
