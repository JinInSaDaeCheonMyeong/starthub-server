package com.jininsadaecheonmyeong.starthubserver.domain.email.repository

import com.jininsadaecheonmyeong.starthubserver.domain.email.entity.Email
import org.springframework.data.jpa.repository.JpaRepository

interface EmailRepository : JpaRepository<Email, Long> {
    fun findByEmail(email: String): Email?

    fun findByEmailAndVerificationCode(
        email: String,
        code: String?,
    ): Email?
}
