package com.jininsadaecheonmyeong.starthubserver.domain.user.repository

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.UserStartupField
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface UserStartupFieldRepository : JpaRepository<UserStartupField, Long> {
    @Modifying
    @Query("DELETE FROM UserStartupField usf WHERE usf.user = :user")
    fun deleteByUser(user: User)

    fun findByUser(user: User): List<UserStartupField>
}
