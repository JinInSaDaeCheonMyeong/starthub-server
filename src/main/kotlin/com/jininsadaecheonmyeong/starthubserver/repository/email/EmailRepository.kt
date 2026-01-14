package com.jininsadaecheonmyeong.starthubserver.repository.email

import com.jininsadaecheonmyeong.starthubserver.entity.email.Email
import org.springframework.data.jpa.repository.JpaRepository

interface EmailRepository : JpaRepository<Email, Long> {
    fun findByEmail(email: String): Email?

    fun findByEmailAndVerificationCode(
        email: String,
        code: String?,
    ): Email?
}
