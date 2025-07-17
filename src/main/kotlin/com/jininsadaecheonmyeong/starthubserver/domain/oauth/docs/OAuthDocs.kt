package com.jininsadaecheonmyeong.starthubserver.domain.oauth.docs

import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.view.RedirectView

@Tag(name = "OAuth", description = "소셜 로그인 관련 API")
interface OAuthDocs {
    @Operation(summary = "state 발급", description = "OAuth를 사용하기 위한 state를 발급합니다.")
    fun generateOAuthState(session: HttpSession): ResponseEntity<BaseResponse<String>>

    @Operation(
        summary = "웹용 구글 로그인/회원가입",
        description = "구글 웹에서 계정으로 로그인/회원가입을 진행하고, 성공 시 설정된 프론트엔드 URI로 리다이렉트합니다.",
        responses = [
            ApiResponse(responseCode = "302", description = "로그인 성공 및 프론트엔드 페이지로 리다이렉트"),
        ],
    )
    fun googleAuthWeb(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession,
        response: HttpServletResponse,
    ): RedirectView

    @Operation(summary = "앱용 구글 로그인/회원가입", description = "구글 앱에서 계정으로 회원가입 및 로그인합니다.")
    fun googleAuthApp(
        @RequestParam code: String,
        @RequestParam state: String,
        @RequestParam platform: String,
        @RequestParam codeVerifier: String,
        session: HttpSession,
    ): ResponseEntity<BaseResponse<OAuthResponse>>

    @Operation(
        summary = "네이버 로그인/회원가입",
        description = "네이버 계정으로 로그인/회원가입을 진행하고, 성공 시 설정된 프론트엔드 URI로 리다이렉트합니다.",
        responses = [
            ApiResponse(responseCode = "302", description = "로그인 성공 및 프론트엔드 페이지로 리다이렉트"),
        ],
    )
    fun naverAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession,
        response: HttpServletResponse,
    ): RedirectView

    @Operation(
        summary = "애플 로그인/회원가입",
        description = "애플 계정으로 로그인/회원가입을 진행하고, 성공 시 설정된 프론트엔드 URI로 리다이렉트합니다.",
        responses = [
            ApiResponse(responseCode = "302", description = "로그인 성공 및 프론트엔드 페이지로 리다이렉트"),
        ],
    )
    fun appleAuth(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession,
        response: HttpServletResponse,
    ): RedirectView
}
