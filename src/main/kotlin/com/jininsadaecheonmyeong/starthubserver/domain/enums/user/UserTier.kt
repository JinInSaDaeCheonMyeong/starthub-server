package com.jininsadaecheonmyeong.starthubserver.domain.enums.user

enum class UserTier(
    val windowTokenLimit: Int,
    val weeklyTokenLimit: Int,
    val unlimited: Boolean = false,
) {
    FREE(
        windowTokenLimit = 50_000,
        weeklyTokenLimit = 200_000,
    ),
    PRO(
        windowTokenLimit = 250_000,
        weeklyTokenLimit = 1_000_000,
    ),
    ADMIN(
        windowTokenLimit = 0,
        weeklyTokenLimit = 0,
        unlimited = true,
    ),
}
