package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.data

import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthUserInfo

data class AppleUserInfo(
    val sub: String,
    override val name: String,
    override val email: String,
    val email_verified: Boolean,
    val given_name: String?,
    val family_name: String?,
    val locale: String?
) : OAuthUserInfo {
    override val id: String get() = sub
    override val profileImage = null
}