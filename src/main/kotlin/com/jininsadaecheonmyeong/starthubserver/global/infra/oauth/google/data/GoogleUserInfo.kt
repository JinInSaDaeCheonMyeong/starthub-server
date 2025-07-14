package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.data

import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthUserInfo

data class GoogleUserInfo(
    override val sub: String,
    override val name: String,
    override val email: String,
    val picture: String?,
    val email_verified: Boolean,
    val given_name: String?,
    val family_name: String?,
    val locale: String?,
) : OAuthUserInfo
