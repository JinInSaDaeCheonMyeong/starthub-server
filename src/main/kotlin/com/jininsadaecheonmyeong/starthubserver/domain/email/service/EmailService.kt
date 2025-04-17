package com.jininsadaecheonmyeong.starthubserver.domain.email.service

import com.jininsadaecheonmyeong.starthubserver.global.configuration.EmailConfig
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.util.FileCopyUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Service
class EmailService (
    private val javaMailSender: JavaMailSender? = null,
    private val emailConfig: EmailConfig? = null
) {

    fun sendEmail(email: String, code: String) {
        try {
            val message: MimeMessage = javaMailSender!!.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            message.setFrom(emailConfig?.username)
            message.setRecipients(MimeMessage.RecipientType.TO, email)
            message.subject = "스타트허브 이메일 인증코드 : $code"

            val resource = ClassPathResource("/static/verify.html")
            val reader = BufferedReader(InputStreamReader(resource.inputStream, StandardCharsets.UTF_8))
            var content = FileCopyUtils.copyToString(reader)
            content = content.replace("{code}", code)

            helper.setText(content, true)

            helper.addInline("image", ClassPathResource("static/스타트허브 인증코드 이미지.png"))

            javaMailSender.send(message)
        } catch (e: MessagingException) {
            logEmailError(email, e)
        } catch (e: IOException) {
            logEmailError(email, e)
        }
    }

    private fun logEmailError(email: String, exception: Exception) {
        logger.error("{}에게 인증 이메일 전송 실패: {}", email, exception.message)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(EmailService::class.java)
    }
}