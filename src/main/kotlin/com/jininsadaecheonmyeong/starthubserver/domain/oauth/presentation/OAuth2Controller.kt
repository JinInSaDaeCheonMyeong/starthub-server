package com.jininsadaecheonmyeong.starthubserver.domain.oauth.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.oauth.service.OAuth2Service
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.enumeration.AuthProvider
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oauth")
class OAuth2Controller(
    private val oAuth2Service: OAuth2Service,
) {
    @GetMapping("/google")
    fun googleAuth(@RequestParam code: String): BaseResponse<TokenResponse?> {
        return BaseResponse.of(oAuth2Service.googleAuth(code, AuthProvider.GOOGLE))
    }

    @GetMapping("/naver")
    fun naverAuth(
        @RequestParam code: String,
        @RequestParam state: String
    ): BaseResponse<TokenResponse?> {
        return BaseResponse.of(oAuth2Service.naverAuth(code, state, AuthProvider.NAVER))
    }
}