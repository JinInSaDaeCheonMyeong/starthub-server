package com.jininsadaecheonmyeong.starthubserver.global.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "google.search")
data class GoogleSearchProperties
    @ConstructorBinding
    constructor(
        val apiKey: String,
        val searchEngineId: String,
        val baseUrl: String = "https://www.googleapis.com/customsearch/v1",
        val timeout: Long = 10000,
    )
