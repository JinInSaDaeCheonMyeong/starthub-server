package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.data

import java.io.Serializable

data class ApplePublicKey(
    val kty: String,
    val kid: String,
    val use: String,
    val alg: String,
    val n: String,
    val e: String,
) : Serializable
