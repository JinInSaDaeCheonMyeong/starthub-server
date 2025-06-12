package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.service

import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidTokenException
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.configuration.GoogleProperties
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.data.GoogleTokenResponse
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.data.GoogleUserInfo
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class GoogleService(
    private val googleProperties: GoogleProperties,
    webClientBuilder: WebClient.Builder
) {
    private val webClient = webClientBuilder.build()

    fun exchangeCodeForUserInfo(
        code: String,
        platform: String,
        codeVerifier: String? = null
    ): GoogleUserInfo {

        val (clientId, redirectUri) = when (platform.lowercase()) {
            "web" -> googleProperties.clientId to googleProperties.redirectUri
            "android" -> googleProperties.androidClientId to googleProperties.androidRedirectUri
            "ios" -> googleProperties.iosClientId to googleProperties.iosRedirectUri
            else -> googleProperties.clientId to googleProperties.redirectUri
        }

        val bodyBuilder = BodyInserters.fromFormData("client_id", clientId)
            .with("code", code)
            .with("redirect_uri", redirectUri)
            .with("grant_type", googleProperties.grantType)

        if (platform == "web" && googleProperties.clientSecret.isNotBlank()) {
            bodyBuilder.with("client_secret", googleProperties.clientSecret)
        }

        if (platform != "web" && !codeVerifier.isNullOrBlank()) {
            bodyBuilder.with("code_verifier", codeVerifier)
        }

        val tokenResponse = webClient.post()
            .uri(googleProperties.tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(bodyBuilder)
            .retrieve()
            .bodyToMono<GoogleTokenResponse>()
            .block()

        val accessToken = tokenResponse?.access_token
            ?: throw InvalidTokenException("유효하지 않은 엑세스 토큰")

        return webClient.get()
            .uri(googleProperties.userInfoUri)
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .bodyToMono<GoogleUserInfo>()
            .block() ?: throw InvalidTokenException("사용자 정보 조회 실패")
    }
}
