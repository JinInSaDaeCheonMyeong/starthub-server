package com.jininsadaecheonmyeong.starthubserver.global.security.filter

import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenParser
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenValidator
import com.jininsadaecheonmyeong.starthubserver.global.security.token.enumeration.TokenType
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.CustomUserDetails
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TokenFilter(
    private val tokenValidator: TokenValidator,
    private val tokenParser: TokenParser,
    private val userRepository: UserRepository,
): OncePerRequestFilter() {
    companion object {
        private const val TOKEN_SECURE_TYPE = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!request.getHeader("Authorization").isNullOrEmpty()) {
            val token: String = request.getHeader("Authorization")?: throw RuntimeException("throw this error when header missing")
            if (!token.startsWith(TOKEN_SECURE_TYPE)) throw RuntimeException("throw this error when token not found")
            tokenValidator.validateAll(token.removePrefix(TOKEN_SECURE_TYPE), TokenType.ACCESS_TOKEN)
            setAuthentication(token.removePrefix(TOKEN_SECURE_TYPE))
        }
        filterChain.doFilter(request, response)
    }

    private fun setAuthentication(token: String) {
        val user = getUserDetails(token)
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(user, null, user.authorities)
    }

    private fun getUserDetails(token: String): CustomUserDetails {
        return CustomUserDetails(userRepository.findByEmail(tokenParser.findEmail(token))?: throw RuntimeException("찾을 수 없는 유저"))
    }
}