package com.jininsadaecheonmyeong.starthubserver.global.infra.discord

import com.jininsadaecheonmyeong.starthubserver.logger
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.web.context.WebServerApplicationContext
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.time.Duration
import java.time.Instant

@Component
class ServerLifecycleDiscordNotifier(
    private val discordWebhookService: DiscordWebhookService,
    private val environment: Environment,
    @param:Value("\${spring.application.name:starthub-server}") private val applicationName: String,
) {
    private val log = logger()
    private val startedAt: Instant = Instant.now()

    @Volatile
    private var serverPort: Int? = null

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady(event: ApplicationReadyEvent) {
        serverPort = (event.applicationContext as? WebServerApplicationContext)?.webServer?.port

        sendStatusNotification(
            title = "🟢 서버 시작",
            color = 5763719,
            extraFields =
                linkedMapOf(
                    "애플리케이션" to applicationName,
                    "프로필" to activeProfiles(),
                    "포트" to (serverPort?.toString() ?: environment.getProperty("server.port", "unknown")),
                    "호스트" to hostName(),
                ),
        )
    }

    @PreDestroy
    fun onShutdown() {
        sendStatusNotification(
            title = "🔴 서버 종료",
            color = 15548997,
            extraFields =
                linkedMapOf(
                    "애플리케이션" to applicationName,
                    "프로필" to activeProfiles(),
                    "포트" to (serverPort?.toString() ?: environment.getProperty("server.port", "unknown")),
                    "업타임" to uptime(),
                    "호스트" to hostName(),
                ),
        )
    }

    private fun sendStatusNotification(
        title: String,
        color: Int,
        extraFields: Map<String, String>,
    ) {
        try {
            discordWebhookService.sendServerStatusNotification(
                title = title,
                color = color,
                fields = extraFields,
            )
        } catch (e: Exception) {
            log.error("서버 상태 알림 전송 실패", e)
        }
    }

    private fun activeProfiles(): String {
        return environment.activeProfiles.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "default"
    }

    private fun hostName(): String {
        return runCatching { InetAddress.getLocalHost().hostName }.getOrDefault("unknown")
    }

    private fun uptime(): String {
        val duration = Duration.between(startedAt, Instant.now())
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        val seconds = duration.toSecondsPart()
        return "%02dh %02dm %02ds".format(hours, minutes, seconds)
    }
}
