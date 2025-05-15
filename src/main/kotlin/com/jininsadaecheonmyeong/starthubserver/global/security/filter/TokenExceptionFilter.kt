package com.jininsadaecheonmyeong.starthubserver.global.security.filter

import com.jininsadaecheonmyeong.starthubserver.logger
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TokenExceptionFilter: OncePerRequestFilter() {
    private val log = logger()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: RuntimeException) { // TODO Custom Exception 으로 변경
            log.error("Exception occurred while processing request", e)
        } catch (e: Exception) {
            log.error("Exception occurred while processing request", e)
        }
    }
}