package com.jininsadaecheonmyeong.starthubserver.domain.repository.document

import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.DocumentEditHistory
import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.GeneratedDocument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DocumentEditHistoryRepository : JpaRepository<DocumentEditHistory, Long> {
    fun findAllByDocumentOrderByCreatedAtDesc(document: GeneratedDocument): List<DocumentEditHistory>
}