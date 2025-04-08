package com.jininsadaecheonmyeong.starthubserver.global.exception

import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.EmailAlreadyExistsException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidPasswordException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
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

    @ExceptionHandler(InvalidPasswordException::class)
    fun handleInvalidPassword(ex: InvalidPasswordException): ResponseEntity<CustomErrorResponse> {
        val customErrorResponse = CustomErrorResponse(
            message = ex.message ?: "잘못된 비밀번호입니다.",
            status = HttpStatus.UNAUTHORIZED.value(),
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(customErrorResponse)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<CustomErrorResponse> {
        val customErrorResponse = CustomErrorResponse(
            message = ex.message ?: "유저를 찾을 수 없습니다.",
            status = HttpStatus.NOT_FOUND.value(),
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(customErrorResponse)
    }
}

data class CustomErrorResponse(
    val message: String,
    val status: Int,
    val timestamp: LocalDateTime = LocalDateTime.now()
)