package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.configuration.AppleProperties
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthUserInfo
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.jackson.io.JacksonDeserializer
import io.jsonwebtoken.jackson.io.JacksonSerializer
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.*

@Service
class AppleService(
    private val appleProperties: AppleProperties,
    private val objectMapper: ObjectMapper
) {
    private val webClient = WebClient.create()

    fun exchangeCodeForUserInfo(code: String): OAuthUserInfo {
        val clientSecret = generateClientSecret()

        val response = webClient.post()
            .uri(appleProperties.tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(
                mapOf(
                    "client_id" to appleProperties.clientId,
                    "client_secret" to clientSecret,
                    "code" to code,
                    "grant_type" to "authorization_code",
                    "redirect_uri" to appleProperties.redirectUri
                )
            )
            .retrieve()
            .bodyToMono(Map::class.java)
            .block() ?: throw IllegalStateException("애플 토큰 응답 실패")

        val idToken = response["id_token"] as? String ?: throw IllegalStateException("id_token 누락")

        return parseIdToken(idToken)
    }

    private fun parseIdToken(idToken: String): OAuthUserInfo {
        val parser = Jwts.parser()
            .json(JacksonDeserializer(objectMapper))
            .build()

        val jwt = parser.parseSignedClaims(idToken)
        val body: Claims = jwt.payload

        val email = body["email"] as? String ?: throw IllegalStateException("이메일 누락")
        val sub = body["sub"] as? String ?: throw IllegalStateException("sub 누락")

        return object : OAuthUserInfo {
            override val id: String = sub
            override val email: String = email
            override val name: String = ""
            override val profileImage: String? = null
        }
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
