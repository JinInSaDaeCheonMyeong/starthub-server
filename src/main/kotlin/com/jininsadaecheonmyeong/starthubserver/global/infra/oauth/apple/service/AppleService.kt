package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.configuration.AppleProperties
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.data.AppleUserInfo
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.jackson.io.JacksonSerializer
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.*

@Service
class AppleService(
    private val appleProperties: AppleProperties,
    private val objectMapper: ObjectMapper,
    webClientBuilder: WebClient.Builder
) {
    private val webClient = webClientBuilder.build()

    fun exchangeCodeForUserInfo(code: String): AppleUserInfo {
        val clientSecret = generateClientSecret()

        val response = webClient.post()
            .uri(appleProperties.tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("client_id", appleProperties.clientId)
                    .with("client_secret", clientSecret)
                    .with("code", code)
                    .with("grant_type", appleProperties.grantType)
                    .with("redirect_uri", appleProperties.redirectUri)
            )
            .retrieve()
            .bodyToMono(Map::class.java)
            .block() ?: throw IllegalStateException("애플 토큰 응답 실패")

        val idToken = response["id_token"] as? String ?: throw IllegalStateException("id_token 누락")

        return parseIdToken(idToken)
    }

    private fun parseIdToken(idToken: String): AppleUserInfo {
        val parts = idToken.split(".")
        if (parts.size != 3) throw IllegalArgumentException("유효하지 않은 JWT 형식")

        val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))
        val payloadMap = objectMapper.readValue(payloadJson, Map::class.java)

        return AppleUserInfo(
            sub = payloadMap["sub"] as? String ?: throw IllegalStateException("sub 누락"),
            name = payloadMap["name"] as? String ?: "",
            email = payloadMap["email"] as? String ?: throw IllegalStateException("이메일 누락"),
            email_verified = (payloadMap["email_verified"] as? Boolean) ?: false,
            given_name = payloadMap["given_name"] as? String,
            family_name = payloadMap["family_name"] as? String,
            locale = payloadMap["locale"] as? String
        )
    }

    private fun generateClientSecret(): String {
        val now = Instant.now()
        val exp = now.plusSeconds(3600)

        val privateKey = parsePrivateKey(appleProperties.privateKey)

        val jwtBuilder = Jwts.builder()
            .json(JacksonSerializer(objectMapper))
            .issuer(appleProperties.teamId)
            .subject(appleProperties.clientId)
            .audience().add("https://appleid.apple.com").and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(privateKey, Jwts.SIG.ES256)
            .header().keyId(appleProperties.keyId).and()

        return jwtBuilder.compact()
    }

    private fun parsePrivateKey(pem: String): PrivateKey {
        val privateKeyPEM = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s+".toRegex(), "")

        val keyBytes = Base64.getDecoder().decode(privateKeyPEM)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePrivate(keySpec)
    }
}
