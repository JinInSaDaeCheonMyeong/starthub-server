package com.jininsadaecheonmyeong.starthubserver.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class ClaudeAIConfig {
    @Value("\${spring.ai.anthropic.api-key}")
    lateinit var apiKey: String

    @Value("\${spring.ai.anthropic.chat.options.model}")
    lateinit var model: String

    @Value("\${spring.ai.anthropic.chat.options.max-tokens}")
    var maxTokens: Int = 4096

    @Value("\${spring.ai.anthropic.chat.options.temperature}")
    var temperature: Double = 0.7
}
