package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.service

import com.jininsadaecheonmyeong.starthubserver.domain.oauth.exception.UnsupportedPlatformException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidTokenException
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.configuration.NaverProperties
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.data.NaverTokenResponse
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.data.NaverUserInfo
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class NaverService(
    private val naverProperties: NaverProperties,
    webClientBuilder: WebClient.Builder
) {
    private val webClient = webClientBuilder.build()

    fun exchangeCodeForUserInfo(
        code: String,
        platform: String,
        codeVerifier: String? = null
    ): NaverUserInfo {

        val (clientId, redirectUri) = when (platform.lowercase()) {
            "web" -> naverProperties.clientId to naverProperties.redirectUri
            "android" -> naverProperties.androidClientId to naverProperties.androidRedirectUri
            "ios" -> naverProperties.iosClientId to naverProperties.iosRedirectUri
            else -> throw UnsupportedPlatformException("지원되지 않는 플랫폼입니다: $platform")
        }

        val bodyBuilder = BodyInserters.fromFormData("client_id", clientId)
            .with("code", code)
            .with("redirect_uri", redirectUri)
            .with("grant_type", naverProperties.grantType)

        if (platform == "web" && naverProperties.clientSecret.isNotBlank()) {
            bodyBuilder.with("client_secret", naverProperties.clientSecret)
        }

        if (platform != "web" && !codeVerifier.isNullOrBlank()) {
            bodyBuilder.with("code_verifier", codeVerifier)
        }

        val tokenResponse = webClient.post()
            .uri(naverProperties.tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(bodyBuilder)
            .retrieve()
            .bodyToMono<NaverTokenResponse>()
            .block() ?: throw InvalidTokenException("네이버 토큰 발급 실패")

        val accessToken = tokenResponse.access_token

        return webClient.get()
            .uri(naverProperties.userInfoUri)
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .bodyToMono<NaverUserInfo>()
            .block() ?: throw InvalidTokenException("사용자 정보 조회 실패")
    }
}