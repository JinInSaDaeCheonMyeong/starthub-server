package com.jininsadaecheonmyeong.starthubserver.global.common

data class BaseResponse<T>(
    val data: T?,
    val status: Int,
    val message: String
) {
    companion object {
        fun <T> ok(data: T?, message: String): BaseResponse<T> =
            BaseResponse(data, 200, message)

        fun <T> created(data: T?, message: String): BaseResponse<T> =
            BaseResponse(data, 201, message)
    }
}