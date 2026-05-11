package com.jininsadaecheonmyeong.starthubserver.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val chatbotRateLimitInterceptor: ChatbotRateLimitInterceptor,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(chatbotRateLimitInterceptor)
            .addPathPatterns("/chatbot/**")
    }
}
