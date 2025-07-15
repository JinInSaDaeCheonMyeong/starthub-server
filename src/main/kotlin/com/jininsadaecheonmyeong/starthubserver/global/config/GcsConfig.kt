package com.jininsadaecheonmyeong.starthubserver.global.config

import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class GcsConfig {
    @Value("\${gcs.credentials-path}")
    private lateinit var credentialsPath: String

    @Bean
    fun storage(): Storage {
        return StorageOptions.newBuilder()
            .setCredentials(
                ServiceAccountCredentials.fromStream(
                    ClassPathResource(credentialsPath).inputStream,
                ),
            )
            .build()
            .service
    }
}
