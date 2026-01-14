package com.jininsadaecheonmyeong.starthubserver.docs.oauth

import com.jininsadaecheonmyeong.starthubserver.dto.request.oauth.AppleAppLoginRequest
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "OAuth", description = "소셜 로그인 관련 API")
interface OAuthDocs {
    @Operation(summary = "state 발급", description = "OAuth를 사용하기 위한 state를 발급합니다.")
    fun generateOAuthState(session: HttpSession): ResponseEntity<BaseResponse<String>>

    @Operation(summary = "앱용 구글 로그인/회원가입", description = "구글 앱에서 계정으로 회원가입 및 로그인합니다.")
    fun googleAuthApp(
        @RequestParam code: String,
        @RequestParam state: String,
        @RequestParam platform: String,
        @RequestParam codeVerifier: String,
        session: HttpSession,
    ): ResponseEntity<BaseResponse<OAuthResponse>>

    @Operation(summary = "앱용 네이버 로그인/회원가입", description = "네이버 앱에서 계정으로 회원가입 및 로그인합니다.")
    fun naverAuthApp(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession,
    ): ResponseEntity<BaseResponse<OAuthResponse>>

    @Operation(summary = "앱용 애플 로그인/회원가입", description = "애플 앱에서 계정으로 회원가입 및 로그인합니다.")
    fun appleAuthApp(
        @RequestBody request: AppleAppLoginRequest,
    ): ResponseEntity<BaseResponse<OAuthResponse>>
}
