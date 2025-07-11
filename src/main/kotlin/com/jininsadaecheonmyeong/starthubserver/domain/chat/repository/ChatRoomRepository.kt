package com.jininsadaecheonmyeong.starthubserver.domain.chat.repository

import com.jininsadaecheonmyeong.starthubserver.domain.chat.entity.ChatRoom
import com.jininsadaecheonmyeong.starthubserver.domain.company.entity.Company
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRoomRepository : JpaRepository<ChatRoom, Long> {
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.company = :company AND cr.user = :user")
    fun findChatRoomByCompanyAndUser(
        company: Company,
        user: User,
    ): ChatRoom?
}
