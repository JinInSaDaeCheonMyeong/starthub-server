package com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.out.persistence

import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.LoadFounderPort
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import org.springframework.stereotype.Component

/**
 * User Persistence Adapter
 * - LoadFounderPort 구현
 * - User 도메인의 Repository를 사용하여 창업자 정보 조회
 */
@Component
class UserPersistenceAdapter(
    private val userRepository: UserRepository
) : LoadFounderPort {

    override fun loadById(founderId: Long): User? {
        return userRepository.findById(founderId).orElse(null)
    }
}
