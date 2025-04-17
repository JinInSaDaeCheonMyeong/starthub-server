package com.jininsadaecheonmyeong.starthubserver.global.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.*

@Configuration
class EmailConfig {
    @Value("\${spring.mail.host}")
    val host: String? = null

    @Value("\${spring.mail.port}")
    val port = 0

    @Value("\${spring.mail.username}")
    val username: String? = null

    @Value("\${spring.mail.password}")
    val password: String? = null

    @Value("\${spring.mail.properties.mail.smtp.auth}")
    val auth = false

    @Value("\${spring.mail.properties.mail.smtp.starttls.enable}")
    val starttlsEnable = false

    @Bean
    fun javaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = host
        mailSender.port = port
        mailSender.username = username
        mailSender.password = password
        mailSender.defaultEncoding = "UTF-8"
        mailSender.javaMailProperties = mailProperties

        return mailSender
    }

    private val mailProperties: Properties
        get() {
            val properties = Properties()
            properties["mail.smtp.auth"] = auth.toString()
            properties["mail.smtp.starttls.enable"] = starttlsEnable.toString()
            properties["mail.smtp.ssl.trust"] = host
            properties["mail.transport.protocol"] = "smtp"
            properties["mail.smtp.port"] = port.toString()

            return properties
        }
}