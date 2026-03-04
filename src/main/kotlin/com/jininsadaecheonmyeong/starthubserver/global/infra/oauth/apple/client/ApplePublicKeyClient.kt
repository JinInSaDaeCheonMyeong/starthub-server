package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.client

import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.data.ApplePublicKey
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.data.ApplePublicKeysResponse
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ApplePublicKeyClient(
    webClientBuilder: WebClient.Builder,
) {
    private val webClient = webClientBuilder.build()

    @Cacheable(cacheNames = ["applePublicKeys"])
    fun getApplePublicKeys(): List<ApplePublicKey> {
        val response =
            webClient.get()
                .uri("https://appleid.apple.com/auth/keys")
                .retrieve()
                .bodyToMono<ApplePublicKeysResponse>()
                .block() ?: throw IllegalStateException("애플 공개키 응답 실패")

        return response.keys
    }
}
