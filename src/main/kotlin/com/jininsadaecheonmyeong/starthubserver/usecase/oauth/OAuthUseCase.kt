package com.jininsadaecheonmyeong.starthubserver.usecase.oauth

import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthResponse

interface OAuthUseCase {
    fun googleAuthApp(
        code: String,
        platform: String,
        codeVerifier: String,
    ): OAuthResponse

    fun naverAuthApp(code: String): OAuthResponse

    fun appleAuthApp(idToken: String): OAuthResponse
}
