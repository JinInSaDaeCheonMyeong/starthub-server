package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.data

import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthUserInfo

data class NaverUserInfo(
    val id: String,
    override val name : String,
    override val email : String
) : OAuthUserInfo {
    override val sub: String get() = id
}