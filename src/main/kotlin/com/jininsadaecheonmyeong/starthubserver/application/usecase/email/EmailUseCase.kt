package com.jininsadaecheonmyeong.starthubserver.application.usecase.email

import com.jininsadaecheonmyeong.starthubserver.domain.entity.email.Email
import com.jininsadaecheonmyeong.starthubserver.domain.exception.email.EmailAlreadyVerifiedException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.email.EmailNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.email.ExpiredEmailException
import com.jininsadaecheonmyeong.starthubserver.domain.repository.email.EmailRepository
import com.jininsadaecheonmyeong.starthubserver.logger
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.core.io.ClassPathResource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.util.FileCopyUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.LocalDateTime

@Component
class EmailUseCase(
    private val emailRepository: EmailRepository,
    private val javaMailSender: JavaMailSender,
    private val mailProperties: MailProperties,
) {
    private val log = logger()

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

        sendEmail(email, code)
    }

    @Transactional
    fun verifyCode(
        email: String,
        code: String,
    ) {
        val verification =
            emailRepository.findByEmailAndVerificationCode(email, code)
                ?: throw EmailNotFoundException("일치하지 않은 인증코드")

        if (verification.expirationDate.isBefore(LocalDateTime.now())) {
            throw ExpiredEmailException("만료된 인증코드")
        } else {
            verification.isVerified = true
            emailRepository.save(verification)
        }
    }

    private fun generateVerificationCode(): String {
        return String.format("%06d", SecureRandom().nextInt(1_000_000))
    }

    @Async
    fun sendEmail(
        email: String,
        code: String,
    ) {
        try {
            val message: MimeMessage = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            message.setFrom(mailProperties.username)
            message.setRecipients(MimeMessage.RecipientType.TO, email)
            message.subject = "스타트허브 이메일 인증코드 : $code"

            val resource = ClassPathResource("/static/verify.html")
            val reader = BufferedReader(InputStreamReader(resource.inputStream, StandardCharsets.UTF_8))
            var content = FileCopyUtils.copyToString(reader)
            content = content.replace("{code}", code)

            helper.setText(content, true)

            helper.addInline("image", ClassPathResource("static/logo.png"))

            javaMailSender.send(message)
        } catch (e: MessagingException) {
            logEmailError(email, e)
        } catch (e: IOException) {
            logEmailError(email, e)
        }
    }

    private fun logEmailError(
        email: String,
        exception: Exception,
    ) {
        log.error("{}에게 인증 이메일 전송 실패: {}", email, exception.message)
    }
}
