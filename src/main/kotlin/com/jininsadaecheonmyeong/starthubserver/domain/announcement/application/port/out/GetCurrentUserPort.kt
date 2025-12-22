package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out

interface GetCurrentUserPort {
    fun getCurrentUserId(): Long
}
