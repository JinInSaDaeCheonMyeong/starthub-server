package com.jininsadaecheonmyeong.starthubserver.domain.email.service

import com.jininsadaecheonmyeong.starthubserver.domain.email.entity.Email
import com.jininsadaecheonmyeong.starthubserver.domain.email.exception.EmailAlreadyVerifiedException
import com.jininsadaecheonmyeong.starthubserver.domain.email.exception.EmailNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.email.exception.ExpiredEmailException
import com.jininsadaecheonmyeong.starthubserver.domain.email.repository.EmailRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.LocalDateTime

@Service
class EmailVerificationService(
    private val emailRepository: EmailRepository,
    private val emailService: EmailService,
) {
    @Transactional
    fun sendVerificationCode(email: String) {
        val existingEmail = emailRepository.findByEmail(email)

        if (existingEmail?.isVerified == true) {
            throw EmailAlreadyVerifiedException("이미 인증된 이메일입니다.")
        }

        val code = generateVerificationCode()

        val emailVerification =
            existingEmail?.apply {
                verificationCode = code
            } ?: Email(
                email = email,
                verificationCode = code,
            )

        emailRepository.save(emailVerification)

        emailService.sendEmail(email, code)
    }

    private fun generateVerificationCode(): String {
        return String.format("%06d", SecureRandom().nextInt(1_000_000))
    }

    fun verifyCode(
        email: String,
        code: String,
    ) {
        val verification = emailRepository.findByEmailAndVerificationCode(email, code) ?: throw EmailNotFoundException("일치하지 않은 인증코드")

        if (verification.expirationDate.isBefore(LocalDateTime.now())) {
            throw ExpiredEmailException("만료된 인증코드")
        } else {
            verification.isVerified = true
            emailRepository.save(verification)
        }
    }
}
