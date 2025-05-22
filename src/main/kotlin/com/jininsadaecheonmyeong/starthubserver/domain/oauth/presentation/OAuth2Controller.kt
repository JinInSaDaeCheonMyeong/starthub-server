package com.jininsadaecheonmyeong.starthubserver.domain.oauth.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.oauth.service.OAuth2Service
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthProvider
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "OAuth", description = "소셜 로그인 관련 API")
@RestController
@RequestMapping("/oauth")
class OAuth2Controller(
    private val oAuth2Service: OAuth2Service,
) {
    @GetMapping("/google")
    fun googleAuth(@RequestParam code: String): ResponseEntity<BaseResponse<TokenResponse>> {
        return BaseResponse.ok(oAuth2Service.googleAuth(code, AuthProvider.GOOGLE), "구글 로그인 성공")
    }

    @GetMapping("/naver")
    fun naverAuth(
        @RequestParam code: String,
        @RequestParam state: String
    ): ResponseEntity<BaseResponse<TokenResponse>> {
        return BaseResponse.ok(oAuth2Service.naverAuth(code, state, AuthProvider.NAVER), "네이버 로그인 성공")
    }

    @PostMapping("/apple")
    fun appleAuth(@RequestParam code: String): ResponseEntity<BaseResponse<TokenResponse>> {
        return BaseResponse.ok(oAuth2Service.appleAuth(code, AuthProvider.APPLE), "애플 로그인 성공")
    }
}