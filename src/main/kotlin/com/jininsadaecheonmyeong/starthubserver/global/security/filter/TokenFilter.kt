package com.jininsadaecheonmyeong.starthubserver.global.security.filter

import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidTokenException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenParser
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenValidator
import com.jininsadaecheonmyeong.starthubserver.global.security.token.enums.TokenType
import com.jininsadaecheonmyeong.starthubserver.global.security.token.exception.ExpiredTokenException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TokenFilter(
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

        if (!request.getHeader("Authorization").isNullOrEmpty()) {
            val token: String =
                request.getHeader("Authorization")
                    ?: throw RuntimeException("헤더가 누락됨")

            if (!token.startsWith(TOKEN_SECURE_TYPE)) {
                throw RuntimeException("토큰을 찾을 수 없음")
            }

            val actualToken = token.removePrefix(TOKEN_SECURE_TYPE)

            try {
                tokenValidator.validateAll(actualToken, TokenType.ACCESS_TOKEN)
                setAuthentication(actualToken)
            } catch (e: Exception) {
                when {
                    isTokenExpiredException(e) -> throw ExpiredTokenException("토큰이 만료되었습니다.")
                    else -> throw InvalidTokenException("유효하지 않은 토큰")
                }
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun setAuthentication(token: String) {
        val userId = tokenParser.findId(token)
        val authorities =
            listOf(
                userRepository.findById(userId.toLong())
                    .orElseThrow { UserNotFoundException("찾을 수 없는 유저") }
                    .let { SimpleGrantedAuthority("ROLE_${"$"}{it.role}") },
            )

        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(userId, null, authorities)
    }

    private fun isTokenExpiredException(e: Exception): Boolean {
        return e.message?.contains("expired", ignoreCase = true) == true ||
            e.message?.contains("만료", ignoreCase = true) == true ||
            e is io.jsonwebtoken.ExpiredJwtException
    }

    private fun isSwaggerPath(requestURI: String): Boolean {
        return requestURI.startsWith("/swagger-ui") || requestURI.startsWith("/v3/api-docs")
    }

    companion object {
        private const val TOKEN_SECURE_TYPE = "Bearer "
    }
}
