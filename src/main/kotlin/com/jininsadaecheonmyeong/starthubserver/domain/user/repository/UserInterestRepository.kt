package com.jininsadaecheonmyeong.starthubserver.domain.user.repository

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.UserInterest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface UserInterestRepository : JpaRepository<UserInterest, Long> {
    @Modifying
    @Query("DELETE FROM UserInterest ui WHERE ui.user = :user")
    fun deleteByUser(user: User)
}
