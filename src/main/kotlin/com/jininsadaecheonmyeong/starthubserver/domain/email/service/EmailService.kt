package com.jininsadaecheonmyeong.starthubserver.domain.email.service

import com.jininsadaecheonmyeong.starthubserver.global.config.EmailConfig
import com.jininsadaecheonmyeong.starthubserver.logger
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
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
    private val log = logger()

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

            helper.addInline("image", ClassPathResource("static/logo.png"))

            javaMailSender.send(message)
        } catch (e: MessagingException) {
            logEmailError(email, e)
        } catch (e: IOException) {
            logEmailError(email, e)
        }
    }

    private fun logEmailError(email: String, exception: Exception) {
        log.error("{}에게 인증 이메일 전송 실패: {}", email, exception.message)
    }
}