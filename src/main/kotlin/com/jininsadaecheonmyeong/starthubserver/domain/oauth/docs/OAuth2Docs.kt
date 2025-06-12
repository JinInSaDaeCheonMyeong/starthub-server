package com.jininsadaecheonmyeong.starthubserver.domain.oauth.docs

import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam

interface OAuth2Docs {

    @Operation(summary = "구글 계정으로 회원가입 및 로그인합니다.")
    fun googleAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        @RequestParam(required = false) platform: String?,
        @RequestParam(required = false) codeVerifier: String?,
        session: HttpSession
    ): ResponseEntity<BaseResponse<OAuthResponse>>

    @Operation(summary = "네이버 계정으로 회원가입 및 로그인합니다.")
    fun naverAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession
    ): ResponseEntity<BaseResponse<OAuthResponse>>


    @Operation(summary = "애플 계정으로 회원가입 및 로그인합니다.")
    fun appleAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession
    ): ResponseEntity<BaseResponse<OAuthResponse>>
}