package com.jininsadaecheonmyeong.starthubserver.global.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
class GcsConfig(
    private val resourceLoader: ResourceLoader,
) {
    @Value("\${gcs.project-id}")
    private lateinit var projectId: String

    @Value("\${gcs.credentials-path:}")
    private val credentialsPath: String? = null

    @Bean
    fun storage(): Storage {
        val credentials =
            if (!credentialsPath.isNullOrEmpty()) {
                val resource = resourceLoader.getResource("classpath:$credentialsPath")
                GoogleCredentials.fromStream(resource.inputStream)
            } else {
                GoogleCredentials.getApplicationDefault()
            }

        return StorageOptions
            .newBuilder()
            .setProjectId(projectId)
            .setCredentials(credentials)
            .build()
            .service
    }
}
