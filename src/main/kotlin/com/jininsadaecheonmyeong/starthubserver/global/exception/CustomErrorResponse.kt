package com.jininsadaecheonmyeong.starthubserver.global.exception

import com.jininsadaecheonmyeong.starthubserver.domain.email.exception.EmailAlreadyVerifiedException
import com.jininsadaecheonmyeong.starthubserver.domain.email.exception.EmailNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.email.exception.EmailNotVerifiedException
import com.jininsadaecheonmyeong.starthubserver.domain.email.exception.ExpiredEmailException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.EmailAlreadyExistsException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidPasswordException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidTokenException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.global.security.token.exception.ExpiredTokenException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<CustomErrorResponse> {
        val errorMessage = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "잘못된 입력 형식"
        val customErrorResponse = CustomErrorResponse(
            message = errorMessage,
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(customErrorResponse)
    }

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleAlreadyExistsEmail(ex: EmailAlreadyExistsException): ResponseEntity<CustomErrorResponse> {
        val customErrorResponse = CustomErrorResponse(
            message = ex.message ?: "이미 등록된 이메일",
            status = HttpStatus.CONFLICT.value(),
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(customErrorResponse)
    }

    @ExceptionHandler(InvalidPasswordException::class)
    fun handleIncorrectPassword(ex: InvalidPasswordException): ResponseEntity<CustomErrorResponse> {
        val customErrorResponse = CustomErrorResponse(
            message = ex.message ?: "잘못된 비밀번호",
            status = HttpStatus.UNAUTHORIZED.value(),
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(customErrorResponse)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<CustomErrorResponse> {
        val customErrorResponse = CustomErrorResponse(
            message = ex.message ?: "유저를 찾을 수 없음",
            status = HttpStatus.NOT_FOUND.value(),
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(customErrorResponse)
    }

    @ExceptionHandler(ExpiredTokenException::class)
    fun handleExpiredToken(ex: ExpiredTokenException): ResponseEntity<CustomErrorResponse> {
        val customErrorResponse = CustomErrorResponse(
            message = ex.message ?: "만료된 토큰",
            status = HttpStatus.UNAUTHORIZED.value(),
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(customErrorResponse)
    }

    @ExceptionHandler(EmailAlreadyVerifiedException::class)
    fun handleAlreadyVerifiedEmail(ex: EmailAlreadyVerifiedException): ResponseEntity<CustomErrorResponse> {
        val customErrorResponse = CustomErrorResponse(
            message = ex.message ?: "이미 인증된 이메일",
            status = HttpStatus.CONFLICT.value(),
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(customErrorResponse)
    }

    @ExceptionHandler(EmailNotVerifiedException::class)
    fun handleNotVerifiedEmail(ex: EmailNotVerifiedException): ResponseEntity<CustomErrorResponse> {
        val customErrorResponse = CustomErrorResponse(
            message = ex.message ?: "인증되지 않은 이메일",
            status = HttpStatus.UNAUTHORIZED.value(),
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(customErrorResponse)
    }

    @ExceptionHandler(ExpiredEmailException::class)
    fun handleExpiredEmail(ex: ExpiredEmailException): ResponseEntity<CustomErrorResponse> {
        val customErrorResponse = CustomErrorResponse(
            message = ex.message ?: "만료된 이메일",
            status = HttpStatus.CONFLICT.value(),
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(customErrorResponse)
    }

    @ExceptionHandler(EmailNotFoundException::class)
    fun handleEmailNotFound(ex: EmailNotFoundException): ResponseEntity<CustomErrorResponse> {
        val customErrorResponse = CustomErrorResponse(
            message = ex.message ?: "이메일을 찾을 수 없음",
            status = HttpStatus.NOT_FOUND.value(),
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(customErrorResponse)
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(ex: InvalidTokenException): ResponseEntity<CustomErrorResponse> {
        val customErrorResponse = CustomErrorResponse(
            message = ex.message ?: "유효하지 않은 토큰",
            status = HttpStatus.UNAUTHORIZED.value(),
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(customErrorResponse)
    }
}

data class CustomErrorResponse(
    val message: String,
    val status: Int,
    val timestamp: LocalDateTime = LocalDateTime.now()
)