package com.jininsadaecheonmyeong.starthubserver.domain.oauth.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.oauth.service.OAuth2Service
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oauth/google")
class OAuth2Controller(
    private val oAuth2Service: OAuth2Service,
) {
    @GetMapping("")
    fun auth(@RequestParam code: String): BaseResponse<TokenResponse> {
        return BaseResponse.of(oAuth2Service.auth(code))
    }
}