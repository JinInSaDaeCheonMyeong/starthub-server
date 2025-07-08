package com.jininsadaecheonmyeong.starthubserver.domain.oauth.docs

import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam

interface OAuthDocs {
    @Operation(summary = "state 발급", description = "OAuth를 사용하기 위한 state를 발급합니다.")
    fun generateOAuthState(session: HttpSession): ResponseEntity<BaseResponse<String>>

    @Operation(summary = "웹용 구글 로그인/회원가입", description = "구글 웹에서 계정으로 회원가입 및 로그인합니다.")
    fun googleAuthWeb(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession,
    ): ResponseEntity<BaseResponse<OAuthResponse>>

    @Operation(summary = "앱용 구글 로그인/회원가입", description = "구글 웹에서 계정으로 회원가입 및 로그인합니다.")
    fun googleAuthApp(
        @RequestParam code: String,
        @RequestParam state: String,
        @RequestParam platform: String,
        @RequestParam codeVerifier: String,
        session: HttpSession,
    ): ResponseEntity<BaseResponse<OAuthResponse>>

    @Operation(summary = "네이버 로그인/회원가입", description = "네이버 계정으로 회원가입 및 로그인합니다.")
    fun naverAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession,
    ): ResponseEntity<BaseResponse<OAuthResponse>>

    @Operation(summary = "애플 로그인/회원가입", description = "애플 계정으로 회원가입 및 로그인합니다.")
    fun appleAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession,
    ): ResponseEntity<BaseResponse<OAuthResponse>>
}
