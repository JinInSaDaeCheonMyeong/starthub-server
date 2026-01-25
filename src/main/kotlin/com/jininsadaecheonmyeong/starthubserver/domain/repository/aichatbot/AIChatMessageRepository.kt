package com.jininsadaecheonmyeong.starthubserver.domain.repository.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot.AIChatMessage
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AIChatMessageRepository : JpaRepository<AIChatMessage, Long> {
    fun findBySessionIdOrderByCreatedAtAsc(sessionId: Long): List<AIChatMessage>

    @Query(
        """
        SELECT m FROM AIChatMessage m
        WHERE m.session.id = :sessionId
        ORDER BY m.createdAt DESC
        """,
    )
    fun findRecentMessages(
        @Param("sessionId") sessionId: Long,
        pageable: Pageable,
    ): List<AIChatMessage>

    fun countBySessionId(sessionId: Long): Long
}
