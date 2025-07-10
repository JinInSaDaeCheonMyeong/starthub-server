package com.jininsadaecheonmyeong.starthubserver.global.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

data class BaseResponse<T>(
    val data: T?,
    val status: HttpStatus,
    val message: String,
) {
    companion object {
        fun <T> of(
            data: T?,
            status: HttpStatus,
            message: String,
        ): ResponseEntity<BaseResponse<T>> =
            ResponseEntity.status(status)
                .body(BaseResponse(data, status, message))

        fun <T> of(
            data: T,
            message: String,
        ): ResponseEntity<BaseResponse<T>> = of(data, HttpStatus.OK, message)

        fun <T> of(
            data: T?,
            status: HttpStatus,
        ): ResponseEntity<BaseResponse<T>> = of(data, status, "")

        fun <T> of(data: T): ResponseEntity<BaseResponse<T>> = of(data, HttpStatus.OK, "")

        fun <T> of(
            message: String,
            status: HttpStatus,
        ): ResponseEntity<BaseResponse<T>> = of(null, status, message)

        fun <T> of(message: String): ResponseEntity<BaseResponse<T>> = of(null, HttpStatus.OK, message)

        fun <T> of(status: HttpStatus): ResponseEntity<BaseResponse<T>> = of(null, status, "")
    }

    fun getStatusCode(): Int = status.value()
}
