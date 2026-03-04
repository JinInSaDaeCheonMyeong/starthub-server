package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.parser

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.client.ApplePublicKeyClient
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.data.ApplePublicKey
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.Base64

@Component
class AppleTokenParser(
    private val applePublicKeyClient: ApplePublicKeyClient,
    private val objectMapper: ObjectMapper,
) {
    fun parseIdToken(idToken: String): Claims {
        val header = parseHeader(idToken)
        val publicKey = getPublicKey(header)
        return Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(idToken).payload
    }

    private fun parseHeader(idToken: String): Map<String, String> {
        val header = idToken.substringBefore(".")
        val decodedHeader = String(Base64.getUrlDecoder().decode(header))
        return objectMapper.readValue(decodedHeader, object : TypeReference<Map<String, String>>() {})
    }

    private fun getPublicKey(header: Map<String, String>): PublicKey {
        val publicKeys = applePublicKeyClient.getApplePublicKeys()
        val matchedKey = findMatchedKey(publicKeys, header["kid"]!!, header["alg"]!!)
        return generatePublicKey(matchedKey)
    }

    private fun findMatchedKey(
        publicKeys: List<ApplePublicKey>,
        kid: String,
        alg: String,
    ): ApplePublicKey {
        return publicKeys.first { it.kid == kid && it.alg == alg }
    }

    private fun generatePublicKey(publicKey: ApplePublicKey): PublicKey {
        val n = BigInteger(1, Base64.getUrlDecoder().decode(publicKey.n))
        val e = BigInteger(1, Base64.getUrlDecoder().decode(publicKey.e))
        val keySpec = RSAPublicKeySpec(n, e)
        val keyFactory = KeyFactory.getInstance(publicKey.kty)
        return keyFactory.generatePublic(keySpec)
    }
}
