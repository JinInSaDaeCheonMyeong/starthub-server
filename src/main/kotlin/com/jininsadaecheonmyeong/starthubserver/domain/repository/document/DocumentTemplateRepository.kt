package com.jininsadaecheonmyeong.starthubserver.domain.repository.document

import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.DocumentTemplate
import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.GeneratedDocument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DocumentTemplateRepository : JpaRepository<DocumentTemplate, Long> {
    fun findByDocument(document: GeneratedDocument): DocumentTemplate?
}
