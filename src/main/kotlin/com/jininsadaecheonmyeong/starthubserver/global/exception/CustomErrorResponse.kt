package com.jininsadaecheonmyeong.starthubserver.global.exception

import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.EmailAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleAlreadyExistsEmail(ex: EmailAlreadyExistsException): ResponseEntity<CustomErrorResponse> {
        val customErrorResponse = CustomErrorResponse(
            message = ex.message ?: "이미 등록된 이메일입니다.",
            status = HttpStatus.CONFLICT.value(),
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(customErrorResponse)
    }
}

data class CustomErrorResponse(
    val message: String,
    val status: Int,
    val timestamp: LocalDateTime = LocalDateTime.now()
)