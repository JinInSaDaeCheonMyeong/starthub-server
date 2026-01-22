package com.jininsadaecheonmyeong.starthubserver.domain.repository.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot.AIChatDocument
import org.springframework.data.jpa.repository.JpaRepository

interface AIChatDocumentRepository : JpaRepository<AIChatDocument, Long> {
    fun findBySessionIdOrderByCreatedAtAsc(sessionId: Long): List<AIChatDocument>

    fun findBySessionId(sessionId: Long): List<AIChatDocument>
}
