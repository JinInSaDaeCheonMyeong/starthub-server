package com.jininsadaecheonmyeong.starthubserver.domain.repository.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot.AIChatSession
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AIChatSessionRepository : JpaRepository<AIChatSession, Long> {
    fun findByUserAndDeletedFalseOrderByUpdatedAtDesc(user: User): List<AIChatSession>

    @Query(
        """
        SELECT DISTINCT s FROM AIChatSession s
        LEFT JOIN FETCH s.messages
        LEFT JOIN FETCH s.documents
        WHERE s.user = :user AND s.deleted = false
        ORDER BY s.updatedAt DESC
        """,
    )
    fun findByUserWithCollections(
        @Param("user") user: User,
    ): List<AIChatSession>

    fun findByIdAndDeletedFalse(id: Long): AIChatSession?

    @Query(
        """
        SELECT s FROM AIChatSession s
        LEFT JOIN FETCH s.messages
        WHERE s.id = :id AND s.deleted = false
        """,
    )
    fun findByIdWithMessages(
        @Param("id") id: Long,
    ): AIChatSession?

    @Query(
        """
        SELECT s FROM AIChatSession s
        LEFT JOIN FETCH s.documents
        WHERE s.id = :id AND s.deleted = false
        """,
    )
    fun findByIdWithDocuments(
        @Param("id") id: Long,
    ): AIChatSession?
}
