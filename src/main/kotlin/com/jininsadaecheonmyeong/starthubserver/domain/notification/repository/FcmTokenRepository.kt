package com.jininsadaecheonmyeong.starthubserver.domain.notification.repository

import com.jininsadaecheonmyeong.starthubserver.domain.notification.entity.FcmToken
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FcmTokenRepository : JpaRepository<FcmToken, Long> {
    fun findByUser(user: User): List<FcmToken>

    fun findByToken(token: String): Optional<FcmToken>

    fun deleteByUser(user: User)

    fun deleteByToken(token: String)
}
