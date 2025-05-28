package com.jininsadaecheonmyeong.starthubserver.domain.oauth.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.user.data.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam

interface Oauth2Docs {

    @Operation(summary = "구글 계정으로 회원가입 및 로그인합니다.",)
    fun googleAuth(@RequestParam code: String): ResponseEntity<BaseResponse<TokenResponse>>

    @Operation(summary = "네이버 계정으로 회원가입 및 로그인합니다.")
    fun naverAuth(
        @RequestParam code: String,
        @RequestParam state: String
    ): ResponseEntity<BaseResponse<TokenResponse>>


    @Operation(summary = "애플 계정으로 회원가입 및 로그인합니다.")
    fun appleAuth(@RequestParam code: String): ResponseEntity<BaseResponse<TokenResponse>>
}