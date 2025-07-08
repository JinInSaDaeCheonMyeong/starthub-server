package com.jininsadaecheonmyeong.starthubserver.global.config

import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.Properties

@Configuration
@EnableConfigurationProperties(MailProperties::class)
class MailConfig(
    private val mailProperties: MailProperties,
) {
    @Bean
    fun javaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()

        mailSender.host = mailProperties.host
        mailSender.port = mailProperties.port
        mailSender.username = mailProperties.username
        mailSender.password = mailProperties.password
        mailSender.defaultEncoding = mailProperties.defaultEncoding?.name() ?: "UTF-8"

        val properties = Properties()
        properties["mail.smtp.auth"] = mailProperties.properties["mail.smtp.auth"] ?: "true"
        properties["mail.smtp.starttls.enable"] = mailProperties.properties["mail.smtp.starttls.enable"] ?: "true"
        properties["mail.smtp.ssl.trust"] = mailProperties.host
        properties["mail.transport.protocol"] = "smtp"
        properties["mail.smtp.port"] = mailProperties.port.toString()

        mailSender.javaMailProperties = properties

        return mailSender
    }
}
