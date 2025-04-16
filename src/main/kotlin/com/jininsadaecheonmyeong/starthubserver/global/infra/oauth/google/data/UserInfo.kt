package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.data

data class UserInfo(
    val sub: String,
    val name: String,
    val email: String,
    val picture: String?,
    val email_verified: Boolean,
    val given_name: String?,
    val family_name: String?,
    val locale: String?
)