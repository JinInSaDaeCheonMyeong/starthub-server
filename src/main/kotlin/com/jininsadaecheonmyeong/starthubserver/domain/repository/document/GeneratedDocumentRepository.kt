package com.jininsadaecheonmyeong.starthubserver.domain.repository.document

import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.GeneratedDocument
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface GeneratedDocumentRepository : JpaRepository<GeneratedDocument, Long> {
    fun findByIdAndDeletedFalse(id: Long): Optional<GeneratedDocument>

    fun findAllByUserAndDeletedFalseOrderByCreatedAtDesc(user: User): List<GeneratedDocument>
}
