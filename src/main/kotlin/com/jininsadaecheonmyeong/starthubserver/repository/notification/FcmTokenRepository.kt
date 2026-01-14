package com.jininsadaecheonmyeong.starthubserver.repository.notification

import com.jininsadaecheonmyeong.starthubserver.entity.notification.FcmToken
import com.jininsadaecheonmyeong.starthubserver.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.enums.notification.DeviceType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FcmTokenRepository : JpaRepository<FcmToken, Long> {
    fun findByUser(user: User): List<FcmToken>

    fun findByToken(token: String): Optional<FcmToken>

    fun findByUserAndDeviceType(
        user: User,
        deviceType: DeviceType,
    ): Optional<FcmToken>

    fun deleteByUser(user: User)

    fun deleteByToken(token: String)
}
