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
import org.springframework.web.reactive.function.client.bodyToMono

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

        return webClient.post()
            .uri("/v1/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .bodyValue(request)
            .retrieve()
            .bodyToFlux<String>()
            .doOnError { log.error("SSE 에러: ${it.message}", it) }
            .flatMapIterable { chunk ->
                chunk.split("\n").filter { it.isNotBlank() }
            }
            .handle { line: String, sink: reactor.core.publisher.SynchronousSink<StreamChunk> ->
                val jsonData =
                    if (line.startsWith("data: ")) {
                        line.removePrefix("data: ").trim()
                    } else {
                        line.trim()
                    }
                if (jsonData.isBlank() || !jsonData.startsWith("{")) {
                    return@handle
                }
                try {
                    val result = parseStreamEvent(jsonData)
                    if (result != null) {
                        sink.next(result)
                    }
                } catch (_: Exception) {
                }
            }
            .takeWhile { it.type != StreamEventType.MESSAGE_STOP }
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

        return webClient.post()
            .uri("/v1/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<ClaudeResponse>()
            .map { response ->
                response.content.firstOrNull()?.text ?: ""
            }
            .block() ?: ""
    }

    suspend fun generateTitle(userMessage: String): String {
        val systemPrompt =
            """
            당신은 채팅 세션 제목 생성기입니다.
            사용자의 메시지를 읽고, 해당 대화의 핵심 주제를 10~25자 이내의 짧은 한국어 제목으로 요약하세요.

            규칙:
            - 반드시 10~25자 이내로 작성
            - 핵심 키워드와 의도를 포함
            - 마침표, 따옴표 등 불필요한 문장부호 제외
            - 제목만 출력하고 다른 설명 없이 바로 제목만 반환

            예시:
            - "배달 앱 수익 모델 질문"
            - "스타트업 법인 설립 방법"
            - "AI 기반 헬스케어 사업 아이디어"
            """.trimIndent()

        val request =
            ClaudeRequest(
                model = "claude-sonnet-4-20250514",
                maxTokens = 50,
                system = systemPrompt,
                messages = listOf(ClaudeMessage(role = "user", content = userMessage)),
                stream = false,
            )

        return try {
            val response =
                webClient.post()
                    .uri("/v1/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono<ClaudeResponse>()
                    .block()

            val title = response?.content?.firstOrNull()?.text?.trim() ?: ""

            if (title.isBlank() || title.length > 50) {
                fallbackTitle(userMessage)
            } else {
                title.replace("\"", "").replace(".", "").trim()
            }
        } catch (_: Exception) {
            fallbackTitle(userMessage)
        }
    }

    private fun fallbackTitle(message: String): String {
        val maxLength = 30
        val cleanMessage = message.replace("\n", " ").replace(Regex("\\s+"), " ").trim()
        return if (cleanMessage.length > maxLength) {
            cleanMessage.take(maxLength - 3) + "..."
        } else {
            cleanMessage
        }
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
        } catch (_: Exception) {
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
    val references: List<Reference>? = null,
)

enum class StreamEventType {
    MESSAGE_START,
    CONTENT_DELTA,
    MESSAGE_DELTA,
    MESSAGE_STOP,
}
