package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.configuration.AppleProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.jackson.io.JacksonSerializer
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.Base64
import java.util.Date

@Component
class AppleClientSecretGenerator(
    private val appleProperties: AppleProperties,
    private val objectMapper: ObjectMapper,
) {
    fun generateClientSecret(): String {
        val now = Instant.now()
        val exp = now.plusSeconds(3600)

        val privateKey = parsePrivateKey(appleProperties.privateKey)

        val jwtBuilder =
            Jwts.builder()
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
        val privateKeyPEM =
            pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s+".toRegex(), "")

        val keyBytes = Base64.getDecoder().decode(privateKeyPEM)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePrivate(keySpec)
    }
}
