package com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot

import com.fasterxml.jackson.annotation.JsonProperty
import com.jininsadaecheonmyeong.starthubserver.application.usecase.aichatbot.ChatHistoryMessage
import com.jininsadaecheonmyeong.starthubserver.domain.enums.aichatbot.MessageRole
import com.jininsadaecheonmyeong.starthubserver.global.config.ClaudeAIConfig
import com.jininsadaecheonmyeong.starthubserver.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux

@Service
class ClaudeAIService(
    private val config: ClaudeAIConfig,
) {
    private val log = logger()

    private val webClient: WebClient by lazy {
        WebClient.builder()
            .baseUrl("https://api.anthropic.com")
            .defaultHeader("x-api-key", config.apiKey)
            .defaultHeader("anthropic-version", "2023-06-01")
            .defaultHeader("Content-Type", "application/json")
            .codecs { it.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) }
            .build()
    }

    fun streamChat(
        systemPrompt: String,
        history: List<ChatHistoryMessage>,
        userMessage: String,
        retrievedContext: String? = null,
    ): Flow<StreamChunk> {
        val messages = buildMessages(history, userMessage, retrievedContext)

        val request =
            ClaudeRequest(
                model = config.model,
                maxTokens = config.maxTokens,
                system = systemPrompt,
                messages = messages,
                stream = true,
            )

        log.info("Claude AI 스트리밍 요청 시작: messages=${messages.size}")

        return webClient.post()
            .uri("/v1/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToFlux<String>()
            .filter { it.isNotBlank() && it.startsWith("data: ") }
            .mapNotNull { line ->
                try {
                    parseStreamEvent(line.removePrefix("data: ").trim())
                } catch (e: Exception) {
                    log.warn("스트림 이벤트 파싱 오류: ${e.message}")
                    null
                }
            }
            .takeWhile { it?.type != StreamEventType.MESSAGE_STOP }
            .asFlow()
    }

    suspend fun chat(
        systemPrompt: String,
        history: List<ChatHistoryMessage>,
        userMessage: String,
        retrievedContext: String? = null,
    ): String {
        val messages = buildMessages(history, userMessage, retrievedContext)

        val request =
            ClaudeRequest(
                model = config.model,
                maxTokens = config.maxTokens,
                system = systemPrompt,
                messages = messages,
                stream = false,
            )

        log.info("Claude AI 요청 시작: messages=${messages.size}")

        return webClient.post()
            .uri("/v1/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ClaudeResponse::class.java)
            .map { response ->
                response.content.firstOrNull()?.text ?: ""
            }
            .block() ?: ""
    }

    private fun buildMessages(
        history: List<ChatHistoryMessage>,
        userMessage: String,
        retrievedContext: String?,
    ): List<ClaudeMessage> {
        val messages = mutableListOf<ClaudeMessage>()

        history.forEach { msg ->
            messages.add(
                ClaudeMessage(
                    role = if (msg.role == MessageRole.USER) "user" else "assistant",
                    content = msg.content,
                ),
            )
        }

        val finalUserMessage =
            if (retrievedContext != null) {
                """
                |참조 정보:
                |$retrievedContext
                |
                |사용자 질문:
                |$userMessage
                """.trimMargin()
            } else {
                userMessage
            }

        messages.add(ClaudeMessage(role = "user", content = finalUserMessage))

        return messages
    }

    private fun parseStreamEvent(data: String): StreamChunk? {
        if (data == "[DONE]") {
            return StreamChunk(type = StreamEventType.MESSAGE_STOP)
        }

        return try {
            val mapper = com.fasterxml.jackson.databind.ObjectMapper()
            val node = mapper.readTree(data)
            val type = node.get("type")?.asText() ?: return null

            when (type) {
                "content_block_delta" -> {
                    val delta = node.get("delta")
                    val text = delta?.get("text")?.asText() ?: ""
                    StreamChunk(type = StreamEventType.CONTENT_DELTA, text = text)
                }
                "message_start" -> {
                    StreamChunk(type = StreamEventType.MESSAGE_START)
                }
                "message_delta" -> {
                    val delta = node.get("delta")
                    val stopReason = delta?.get("stop_reason")?.asText()
                    StreamChunk(type = StreamEventType.MESSAGE_DELTA, stopReason = stopReason)
                }
                "message_stop" -> {
                    StreamChunk(type = StreamEventType.MESSAGE_STOP)
                }
                else -> null
            }
        } catch (e: Exception) {
            log.warn("JSON 파싱 오류: ${e.message}")
            null
        }
    }

    data class ClaudeRequest(
        val model: String,
        @JsonProperty("max_tokens")
        val maxTokens: Int,
        val system: String,
        val messages: List<ClaudeMessage>,
        val stream: Boolean,
    )

    data class ClaudeMessage(
        val role: String,
        val content: String,
    )

    data class ClaudeResponse(
        val id: String,
        val type: String,
        val role: String,
        val content: List<ContentBlock>,
        val model: String,
        @JsonProperty("stop_reason")
        val stopReason: String?,
    )

    data class ContentBlock(
        val type: String,
        val text: String,
    )
}

data class StreamChunk(
    val type: StreamEventType,
    val text: String? = null,
    val stopReason: String? = null,
)

enum class StreamEventType {
    MESSAGE_START,
    CONTENT_DELTA,
    MESSAGE_DELTA,
    MESSAGE_STOP,
}
