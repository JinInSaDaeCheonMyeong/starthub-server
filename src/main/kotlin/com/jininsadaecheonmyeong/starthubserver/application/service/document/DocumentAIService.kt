package com.jininsadaecheonmyeong.starthubserver.application.service.document

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.jininsadaecheonmyeong.starthubserver.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class DocumentAIService(
    @param:Value("\${spring.ai.openai.api-key}") private val apiKey: String,
    @param:Value("\${spring.ai.openai.chat.options.model:gpt-4o-mini}") private val model: String,
) {
    private val log = logger()

    private val webClient: WebClient by lazy {
        WebClient.builder()
            .baseUrl("https://api.openai.com")
            .defaultHeader("Authorization", "Bearer $apiKey")
            .defaultHeader("Content-Type", "application/json")
            .codecs { it.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) }
            .build()
    }

    fun chat(
        systemPrompt: String,
        userMessage: String,
    ): String {
        val request = OpenAIRequest(
            model = model,
            messages = listOf(
                OpenAIMessage(role = "system", content = systemPrompt),
                OpenAIMessage(role = "user", content = userMessage),
            ),
            maxTokens = 4096,
            temperature = 0.7,
        )

        return try {
            val response = webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono<OpenAIResponse>()
                .block()

            response?.choices?.firstOrNull()?.message?.content ?: ""
        } catch (e: Exception) {
            log.error("OpenAI API 호출 실패: ${e.message}", e)
            ""
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class OpenAIRequest(
        val model: String,
        val messages: List<OpenAIMessage>,
        @param:JsonProperty("max_tokens")
        val maxTokens: Int,
        val temperature: Double,
    )

    data class OpenAIMessage(
        val role: String,
        val content: String,
    )

    data class OpenAIResponse(
        val choices: List<Choice>,
    )

    data class Choice(
        val message: ResponseMessage,
    )

    data class ResponseMessage(
        val content: String,
    )
}