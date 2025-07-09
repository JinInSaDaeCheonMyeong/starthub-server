package com.jininsadaecheonmyeong.starthubserver.global.exception

import com.jininsadaecheonmyeong.starthubserver.global.infra.discord.DiscordWebhookService
import com.jininsadaecheonmyeong.starthubserver.logger
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler(
    private val discordWebhookService: DiscordWebhookService,
) {
    private val logger = logger()

    private val excludedExceptionTypes =
        setOf(
            CustomException::class.java,
            IllegalArgumentException::class.java,
            NoResourceFoundException::class.java,
        )

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(ex: CustomException): ResponseEntity<CustomErrorResponse> {
        logger.warn("CustomException 발생: {}", ex.message)

        val response =
            CustomErrorResponse(
                message = ex.message,
                status = ex.status.value(),
            )
        return ResponseEntity.status(ex.status).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<CustomErrorResponse> {
        val errorMessage = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "잘못된 입력 형식"
        logger.warn("Validation 오류: {}", errorMessage)

        val response =
            CustomErrorResponse(
                message = errorMessage,
                status = HttpStatus.BAD_REQUEST.value(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn("잘못된 요청: {}", ex.message)

        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = ex.message ?: "잘못된 요청입니다.",
                path = request.requestURI,
            )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.warn("존재하지 않는 리소스 요청: {}", request.requestURI)

        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.NOT_FOUND.value(),
                error = "Not Found",
                message = "요청하신 리소스를 찾을 수 없습니다.",
                path = request.requestURI,
            )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val shouldNotify = !isExcludedException(ex)

        if (shouldNotify) {
            logger.error("시스템 오류 발생", ex)
            sendDiscordNotification(ex, request)
        } else {
            logger.warn("클라이언트 오류 발생: {} - {}", ex.javaClass.simpleName, ex.message)
        }

        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = "서버에서 오류가 발생했습니다.",
                path = request.requestURI,
            )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    private fun isExcludedException(ex: Exception): Boolean {
        return excludedExceptionTypes.any { excludedType ->
            excludedType.isAssignableFrom(ex.javaClass)
        }
    }

    private fun sendDiscordNotification(
        ex: Exception,
        request: HttpServletRequest,
    ) {
        val userId =
            try {
                SecurityContextHolder.getContext().authentication?.name
            } catch (_: Exception) {
                null
            }

        val clientIp = getClientIpAddress(request)

        try {
            discordWebhookService.sendErrorNotification(
                error = ex,
                requestUri = request.requestURI,
                userId = userId,
                additionalInfo =
                    mapOf(
                        "HTTP Method" to request.method,
                        "클라이언트 IP" to clientIp,
                        "User Agent" to (request.getHeader("User-Agent") ?: "Unknown"),
                        "Referer" to (request.getHeader("Referer") ?: "직접 접속"),
                        "Exception Type" to ex.javaClass.simpleName,
                    ),
            )
        } catch (discordError: Exception) {
            logger.error("Discord 알림 전송 실패", discordError)
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

        val xOriginalForwardedFor = request.getHeader("X-Original-Forwarded-For")
        if (!xOriginalForwardedFor.isNullOrBlank()) {
            return xOriginalForwardedFor.split(",")[0].trim()
        }

        val cfConnectingIp = request.getHeader("CF-Connecting-IP")
        if (!cfConnectingIp.isNullOrBlank()) {
            return cfConnectingIp.trim()
        }

        return request.remoteAddr ?: "Unknown"
    }
}

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
)
