package com.jininsadaecheonmyeong.starthubserver.global.common

import org.springframework.data.domain.Page

data class CustomPageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalElements: Long,
    val isLast: Boolean,
) {
    companion object {
        fun <T> from(page: Page<T>): CustomPageResponse<T> {
            return CustomPageResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalPages = page.totalPages,
                totalElements = page.totalElements,
                isLast = page.isLast,
            )
        }
    }
}
