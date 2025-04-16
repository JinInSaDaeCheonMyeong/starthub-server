package com.jininsadaecheonmyeong.starthubserver

import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.configuration.GoogleProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@EnableConfigurationProperties(GoogleProperties::class)
@SpringBootApplication
class StartHubServerApplication

fun main(args: Array<String>) {
    runApplication<StartHubServerApplication>(*args)
}
