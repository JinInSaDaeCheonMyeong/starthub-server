package com.jininsadaecheonmyeong.starthubserver.global.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
class FirebaseConfig(
    private val resourceLoader: ResourceLoader,
) {
    @Value("\${firebase.credentials-path:}")
    private val credentialsPath: String? = null

    @PostConstruct
    fun initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            val credentials =
                if (!credentialsPath.isNullOrEmpty()) {
                    val resource = resourceLoader.getResource("classpath:$credentialsPath")
                    GoogleCredentials.fromStream(resource.inputStream)
                } else {
                    GoogleCredentials.getApplicationDefault()
                }

            val options =
                FirebaseOptions
                    .builder()
                    .setCredentials(credentials)
                    .build()

            FirebaseApp.initializeApp(options)
        }
    }
}
