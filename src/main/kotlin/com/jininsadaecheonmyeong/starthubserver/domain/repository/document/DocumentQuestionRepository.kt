package com.jininsadaecheonmyeong.starthubserver.domain.repository.document

import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.DocumentQuestion
import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.GeneratedDocument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DocumentQuestionRepository : JpaRepository<DocumentQuestion, Long> {
    fun findAllByDocumentOrderByOrderIndexAsc(document: GeneratedDocument): List<DocumentQuestion>
}