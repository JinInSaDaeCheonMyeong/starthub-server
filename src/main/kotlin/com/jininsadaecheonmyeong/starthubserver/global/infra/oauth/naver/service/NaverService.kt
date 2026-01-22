package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.service

import com.jininsadaecheonmyeong.starthubserver.domain.exception.user.InvalidTokenException
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
    webClientBuilder: WebClient.Builder,
) {
    private val webClient = webClientBuilder.build()

    fun exchangeCodeForUserInfoApp(code: String): NaverUserInfo {
        val tokenResponse =
            webClient.post()
                .uri(naverProperties.tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                    BodyInserters.fromFormData("client_id", naverProperties.clientId)
                        .with("client_secret", naverProperties.clientSecret)
                        .with("code", code)
                        .with("grant_type", naverProperties.grantType),
                )
                .retrieve()
                .bodyToMono<NaverTokenResponse>()
                .block() ?: throw InvalidTokenException("네이버 토큰 발급 실패")

        val accessToken = tokenResponse.access_token

        val userInfo =
            webClient.get()
                .uri(naverProperties.userInfoUri)
                .headers { it.setBearerAuth(accessToken) }
                .retrieve()
                .bodyToMono<NaverUserInfo>()
                .block() ?: throw InvalidTokenException("사용자 정보 조회 실패")

        return userInfo
    }
}
