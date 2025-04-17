package com.jininsadaecheonmyeong.starthubserver.global.common

data class BaseResponse<T>(
    val data: T,
    val status: Int,
    val message: String
) {
    companion object {
        fun <T> of(data: T?): BaseResponse<T?> {
            return BaseResponse(
                data = data,
                message = "성공",
                status = 200
            )
        }
    }
}