package com.jininsadaecheonmyeong.starthubserver.global.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(PerplexityProperties::class)
class SearchConfiguration {
    @Bean("perplexityWebClient")
    fun perplexityWebClient(properties: PerplexityProperties): WebClient {
        return WebClient.builder()
            .baseUrl(properties.baseUrl)
            .defaultHeader("Authorization", "Bearer ${properties.apiKey}")
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("User-Agent", "StartHub-Server/1.0")
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) // 2MB
            }
            .build()
    }

    @Bean("companyInfoWebClient")
    fun companyInfoWebClient(): WebClient {
        return WebClient.builder()
            .defaultHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            )
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) // 2MB
            }
            .build()
    }
}
