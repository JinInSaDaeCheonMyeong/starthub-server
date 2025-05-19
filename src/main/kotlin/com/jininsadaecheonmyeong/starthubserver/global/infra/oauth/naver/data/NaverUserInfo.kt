package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.data

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthProvider
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthUserInfo

data class NaverUserInfo(
    val response: NaverProfile
)

data class NaverProfile(
    override val id: String,
    override val email: String,
    override val name: String,
    val profile_image: String?
) : OAuthUserInfo {
    override val profileImage: String? get() = profile_image
    override val provider = AuthProvider.NAVER
}