package com.jininsadaecheonmyeong.starthubserver.global.security.filter

import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidTokenException
import com.jininsadaecheonmyeong.starthubserver.global.security.token.exception.ExpiredTokenException
import com.jininsadaecheonmyeong.starthubserver.logger
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
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
        } catch (e: ExpiredTokenException) {
            log.error("Token expired", e)
            handleTokenException(response, HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", e.message ?: "토큰이 만료되었습니다.")
        } catch (e: InvalidTokenException) {
            log.error("Invalid token", e)
            handleTokenException(response, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", e.message ?: "유효하지 않은 토큰입니다.")
        } catch (e: RuntimeException) {
            log.error("Runtime exception occurred while processing request", e)
            when {
                e.message?.contains("header missing") == true -> 
                    handleTokenException(response, HttpStatus.UNAUTHORIZED, "MISSING_HEADER", "Authorization 헤더가 없습니다.")
                e.message?.contains("token not found") == true -> 
                    handleTokenException(response, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_FORMAT", "Bearer 토큰 형식이 잘못되었습니다.")
                e.message?.contains("찾을 수 없는 유저") == true -> 
                    handleTokenException(response, HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "토큰에 해당하는 사용자를 찾을 수 없습니다.")
                else -> 
                    handleTokenException(response, HttpStatus.INTERNAL_SERVER_ERROR, "AUTHENTICATION_ERROR", "인증 처리 중 오류가 발생했습니다.")
            }
        } catch (e: Exception) {
            log.error("Exception occurred while processing request", e)
            handleTokenException(response, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.")
        }
    }

    private fun handleTokenException(
        response: HttpServletResponse,
        status: HttpStatus,
        errorCode: String,
        message: String
    ) {
        response.status = status.value()
        response.contentType = "application/json;charset=UTF-8"
        
        val errorResponse = """
            {
                "error": "$errorCode",
                "message": "$message",
                "status": ${status.value()},
                "timestamp": "${java.time.LocalDateTime.now()}"
            }
        """.trimIndent()
        
        response.writer.write(errorResponse)
        response.writer.flush()
    }
}