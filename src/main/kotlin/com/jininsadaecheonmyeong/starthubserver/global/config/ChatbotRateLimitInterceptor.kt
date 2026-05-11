package com.jininsadaecheonmyeong.starthubserver.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.BanService
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.RateLimitService
import com.jininsadaecheonmyeong.starthubserver.domain.repository.user.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomErrorResponse
import com.jininsadaecheonmyeong.starthubserver.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ChatbotRateLimitInterceptor(
    private val rateLimitService: RateLimitService,
    private val banService: BanService,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper,
) : HandlerInterceptor {
    private val log = logger()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val userId = getCurrentUserId() ?: return true
        val clientIp = getClientIpAddress(request)

        val user = userRepository.findById(userId).orElse(null) ?: return true

        if (user.banned) {
            writeErrorResponse(
                response,
                HttpStatus.FORBIDDEN,
                "계정이 차단되었습니다. 관리자에게 문의해주세요.",
            )
            return false
        }

        if (user.chatbotBanned && request.method != HttpMethod.GET.name()) {
            log.warn(
                "Chatbot ban으로 차단: userId={}, method={}, uri={}",
                userId,
                request.method,
                request.requestURI,
            )
            writeErrorResponse(
                response,
                HttpStatus.FORBIDDEN,
                "부적절하거나 악의적인 챗봇 기능 사용으로 사용이 제한되었습니다.",
            )
            return false
        }

        if (rateLimitService.isIpRateLimited(clientIp)) {
            log.warn("IP Rate Limit 초과: ip={}, userId={}", clientIp, userId)
            writeErrorResponse(
                response,
                HttpStatus.TOO_MANY_REQUESTS,
                "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.",
            )
            return false
        }

        if (rateLimitService.isRateLimited(userId)) {
            val violations = rateLimitService.recordViolation(userId)
            log.warn("User Rate Limit 초과: userId={}, violations={}", userId, violations)

            if (banService.checkAndBanIfAbusive(user, clientIp)) {
                writeErrorResponse(
                    response,
                    HttpStatus.FORBIDDEN,
                    "비정상적인 사용이 감지되어 계정이 차단되었습니다.",
                )
                return false
            }

            writeErrorResponse(
                response,
                HttpStatus.TOO_MANY_REQUESTS,
                "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.",
            )
            return false
        }

        return true
    }

    private fun getCurrentUserId(): Long? {
        return try {
            val principal = SecurityContextHolder.getContext().authentication?.principal as? String
            principal?.toLongOrNull()
        } catch (_: Exception) {
            null
        }
    }

    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",")[0].trim()
        }
        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp.trim()
        }
        val cfConnectingIp = request.getHeader("CF-Connecting-IP")
        if (!cfConnectingIp.isNullOrBlank()) {
            return cfConnectingIp.trim()
        }
        return request.remoteAddr ?: "Unknown"
    }

    private fun writeErrorResponse(
        response: HttpServletResponse,
        status: HttpStatus,
        message: String,
    ) {
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        val errorResponse =
            CustomErrorResponse(
                message = message,
                status = status.value(),
            )
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
