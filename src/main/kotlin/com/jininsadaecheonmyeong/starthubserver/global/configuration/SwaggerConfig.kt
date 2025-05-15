package com.jininsadaecheonmyeong.starthubserver.global.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun api(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("StartHub-Server API")
                    .description("Kotlin API")
                    .version("1.0.0")
            )
            .addSecurityItem(
                SecurityRequirement()
                    .addList("Authorization")
            )
            .servers(
                listOf(
                    Server().url("https://api.start-hub.kr").description("Production Server"),
                    Server().url("http://localhost:8080").description("Development Server")
                )
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "Authorization",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("Authorization")
                            .`in`(SecurityScheme.In.HEADER)
                            .name("Authorization")
                    )
            )
    }
}