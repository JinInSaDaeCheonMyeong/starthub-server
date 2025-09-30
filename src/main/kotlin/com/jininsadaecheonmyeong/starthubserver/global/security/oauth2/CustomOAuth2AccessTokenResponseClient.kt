package com.jininsadaecheonmyeong.starthubserver.global.security.oauth2

import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.service.AppleClientSecretGenerator
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@Component
class CustomOAuth2AccessTokenResponseClient(
    private val appleClientSecretGenerator: AppleClientSecretGenerator,
    private val webClient: WebClient,
) : OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
    private val defaultClient = RestClientAuthorizationCodeTokenResponseClient()

    override fun getTokenResponse(request: OAuth2AuthorizationCodeGrantRequest): OAuth2AccessTokenResponse {
        if (request.clientRegistration.registrationId != "apple") {
            return defaultClient.getTokenResponse(request)
        }

        val clientSecret = appleClientSecretGenerator.generateClientSecret()
        val clientRegistration = request.clientRegistration

        val body = LinkedMultiValueMap<String, String>()
        body.add("client_id", clientRegistration.clientId)
        body.add("client_secret", clientSecret)
        body.add("code", request.authorizationExchange.authorizationResponse.code)
        body.add("grant_type", request.grantType.value)
        body.add("redirect_uri", request.authorizationExchange.authorizationRequest.redirectUri)

        val responseMap =
            webClient.post()
                .uri(clientRegistration.providerDetails.tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(Map::class.java)
                .block() ?: throw RuntimeException("Failed to get token response from Apple")

        val accessToken = responseMap["access_token"] as String
        val expiresIn = (responseMap["expires_in"] as Number).toLong()
        val refreshToken = responseMap["refresh_token"] as String
        val idToken = responseMap["id_token"] as String

        val scopes =
            if (responseMap.containsKey("scope")) {
                (responseMap["scope"] as String).split(" ").toSet()
            } else {
                clientRegistration.scopes
            }

        return OAuth2AccessTokenResponse.withToken(accessToken)
            .tokenType(OAuth2AccessToken.TokenType.BEARER)
            .expiresIn(expiresIn)
            .refreshToken(refreshToken)
            .scopes(scopes)
            .additionalParameters(mapOf("id_token" to idToken))
            .build()
    }
}
