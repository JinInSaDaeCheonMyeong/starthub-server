package com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User

/**
 * Founder(User) 정보를 조회하는 Out Port
 * - 향후 User 도메인도 헥사고날로 전환 시 User Domain Model로 변경 예정
 * - 현재는 기존 User Entity 사용 (과도기적 접근)
 */
interface LoadFounderPort {
    fun loadById(founderId: Long): User?
}
