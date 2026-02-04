package com.jininsadaecheonmyeong.starthubserver.global.infra.discord

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class DiscordWebhookService(
    @param:Value("\${discord.webhook.url}") private val webhookUrl: String,
    private val webClient: WebClient,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun sendErrorNotification(
        error: Throwable,
        requestUri: String? = null,
        userId: String? = null,
        additionalInfo: Map<String, Any>? = null,
    ) {
        try {
            val embed = createErrorEmbed(error, requestUri, userId, additionalInfo)

            webClient.post()
                .uri(webhookUrl)
                .bodyValue(mapOf("embeds" to listOf(embed)))
                .retrieve()
                .bodyToMono(String::class.java)
                .doOnSuccess { logger.info("Discord 알림 전송 성공") }
                .doOnError { logger.error("Discord 알림 전송 실패", it) }
                .onErrorResume { Mono.empty() }
                .subscribe()
        } catch (e: Exception) {
            logger.error("Discord 웹훅 전송 중 오류 발생", e)
        }
    }

    private fun createErrorEmbed(
        error: Throwable,
        requestUri: String?,
        userId: String?,
        additionalInfo: Map<String, Any>?,
    ): Map<String, Any> {
        val koreaTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        val koreaTimeString = koreaTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val isoTimestamp = koreaTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val fields = mutableListOf<Map<String, Any>>()

        fields.add(
            mapOf(
                "name" to "에러 타입",
                "value" to error.javaClass.simpleName,
                "inline" to true,
            ),
        )

        requestUri?.let {
            fields.add(
                mapOf(
                    "name" to "엔드포인트",
                    "value" to it,
                    "inline" to true,
                ),
            )
        }

        userId?.let {
            fields.add(
                mapOf(
                    "name" to "사용자 ID",
                    "value" to it,
                    "inline" to true,
                ),
            )
        }

        error.message?.let { message ->
            fields.add(
                mapOf(
                    "name" to "에러 내용",
                    "value" to if (message.length > 1000) message.substring(0, 1000) + "..." else message,
                    "inline" to false,
                ),
            )
        }

        val stackTrace =
            error.stackTrace.take(5).joinToString("\n") {
                "at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
            }
        if (stackTrace.isNotEmpty()) {
            fields.add(
                mapOf(
                    "name" to "Stack Trace",
                    "value" to "```\n$stackTrace\n```",
                    "inline" to false,
                ),
            )
        }

        additionalInfo?.forEach { (key, value) ->
            fields.add(
                mapOf(
                    "name" to key,
                    "value" to value.toString(),
                    "inline" to true,
                ),
            )
        }

        return mapOf(
            "title" to "🚨 서버 에러 발생",
            "color" to 15158332,
            "fields" to fields,
            "footer" to mapOf("text" to "발생 시간: $koreaTimeString (KST)"),
            "timestamp" to isoTimestamp,
        )
    }
}
