package com.jininsadaecheonmyeong.starthubserver.global.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "perplexity")
data class PerplexityProperties
    @ConstructorBinding
    constructor(
        val apiKey: String,
        val baseUrl: String = "https://api.perplexity.ai",
        val model: String = "llama-3.1-sonar-large-128k-online",
        val timeout: Long = 60000,
        val maxTokens: Int = 10000,
    )
