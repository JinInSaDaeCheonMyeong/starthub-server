package com.jininsadaecheonmyeong.starthubserver.global.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

data class BaseResponse<T>(
    val data: T?,
    val status: HttpStatus,
    val message: String
) {
    companion object {
        fun <T> of(data: T?, status: HttpStatus, message: String): ResponseEntity<BaseResponse<T>> =
            ResponseEntity.status(status)
                .body(BaseResponse(data, status, message))

        fun <T> ok(data: T?, message: String): ResponseEntity<BaseResponse<T>> =
            of(data, HttpStatus.OK, message)

        fun <T> created(data: T?, message: String): ResponseEntity<BaseResponse<T>> =
            of(data, HttpStatus.CREATED, message)
            
        fun <T> badRequest(data: T?, message: String): ResponseEntity<BaseResponse<T>> =
            of(data, HttpStatus.BAD_REQUEST, message)
            
        fun <T> notFound(data: T?, message: String): ResponseEntity<BaseResponse<T>> =
            of(data, HttpStatus.NOT_FOUND, message)
            
        fun <T> forbidden(data: T?, message: String): ResponseEntity<BaseResponse<T>> =
            of(data, HttpStatus.FORBIDDEN, message)
            
        fun <T> unauthorized(data: T?, message: String): ResponseEntity<BaseResponse<T>> =
            of(data, HttpStatus.UNAUTHORIZED, message)
            
        fun <T> internalServerError(data: T?, message: String): ResponseEntity<BaseResponse<T>> =
            of(data, HttpStatus.INTERNAL_SERVER_ERROR, message)
    }

    fun getStatusCode(): Int = status.value()
}