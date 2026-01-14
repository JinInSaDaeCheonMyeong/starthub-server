package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.service

import com.jininsadaecheonmyeong.starthubserver.exception.user.InvalidTokenException
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
    webClientBuilder: WebClient.Builder,
) {
    private val webClient = webClientBuilder.build()

    fun exchangeCodeForUserInfoApp(
        code: String,
        platform: String,
        codeVerifier: String,
    ): GoogleUserInfo {
        val (clientId, redirectUri) =
            when (platform.lowercase()) {
                "android" -> googleProperties.androidClientId to googleProperties.androidRedirectUri
                "ios" -> googleProperties.iosClientId to googleProperties.iosRedirectUri
                else -> throw IllegalArgumentException("지원하지 않는 플랫폼입니다.")
            }
        val bodyBuilder = createTokenRequestBody(clientId, code, redirectUri, codeVerifier = codeVerifier)
        return getUserInfoFromToken(bodyBuilder)
    }

    private fun createTokenRequestBody(
        clientId: String,
        code: String,
        redirectUri: String,
        codeVerifier: String?,
    ) = BodyInserters.fromFormData("client_id", clientId)
        .with("code", code)
        .with("redirect_uri", redirectUri)
        .with("grant_type", googleProperties.grantType)
        .also {
            if (!codeVerifier.isNullOrBlank()) it.with("code_verifier", codeVerifier)
        }

    private fun getUserInfoFromToken(bodyBuilder: BodyInserters.FormInserter<String>): GoogleUserInfo {
        val tokenResponse =
            webClient.post()
                .uri(googleProperties.tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(bodyBuilder)
                .retrieve()
                .bodyToMono<GoogleTokenResponse>()
                .block()

        val accessToken =
            tokenResponse?.access_token
                ?: throw InvalidTokenException("유효하지 않은 엑세스 토큰")

        return webClient.get()
            .uri(googleProperties.userInfoUri)
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .bodyToMono<GoogleUserInfo>()
            .block() ?: throw InvalidTokenException("사용자 정보 조회 실패")
    }
}
