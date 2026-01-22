package com.jininsadaecheonmyeong.starthubserver.global.security.filter

import com.jininsadaecheonmyeong.starthubserver.domain.exception.user.InvalidTokenException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.user.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.repository.user.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenParser
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenValidator
import com.jininsadaecheonmyeong.starthubserver.global.security.token.enums.TokenType
import com.jininsadaecheonmyeong.starthubserver.global.security.token.exception.ExpiredTokenException
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class PlatformAuthenticationFilter(
    private val tokenValidator: TokenValidator,
    private val tokenParser: TokenParser,
    private val userRepository: UserRepository,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (isSwaggerPath(request.requestURI)) {
            filterChain.doFilter(request, response)
            return
        }

        val platform = request.getHeader(PLATFORM_HEADER)

        val token =
            when (platform) {
                WEB_PLATFORM -> {
                    extractTokenFromCookie(request)
                }

                APP_PLATFORM -> {
                    extractTokenFromHeader(request)
                }

                else -> {
                    extractTokenFromHeader(request) ?: extractTokenFromCookie(request)
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
        val userId = tokenParser.findId(token)
        val authorities =
            listOf(
                userRepository.findById(userId.toLong())
                    .orElseThrow { UserNotFoundException("찾을 수 없는 유저") }
                    .let { SimpleGrantedAuthority("ROLE_${it.role}") },
            )

        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(userId, null, authorities)
    }

    private fun isTokenExpiredException(e: Exception): Boolean {
        return e.message?.contains("expired", ignoreCase = true) == true ||
            e.message?.contains("만료", ignoreCase = true) == true ||
            e is ExpiredJwtException
    }

    private fun isSwaggerPath(requestURI: String): Boolean {
        return requestURI.startsWith("/swagger-ui") || requestURI.startsWith("/v3/api-docs")
    }

    companion object {
        private const val TOKEN_SECURE_TYPE = "Bearer "
        private const val ACCESS_TOKEN_COOKIE_NAME = "access_token"
        private const val PLATFORM_HEADER = "X-Platform"
        private const val WEB_PLATFORM = "web"
        private const val APP_PLATFORM = "app"
    }
}
