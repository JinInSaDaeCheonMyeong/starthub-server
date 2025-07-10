package com.jininsadaecheonmyeong.starthubserver.domain.oauth.service

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthType
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.service.AppleService
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthResponse
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthUserInfo
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.service.GoogleService
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.service.NaverService
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenProvider
import org.springframework.stereotype.Service

@Service
class OAuthService(
    private val tokenProvider: TokenProvider,
    private val googleService: GoogleService,
    private val naverService: NaverService,
    private val appleService: AppleService,
    private val userRepository: UserRepository,
) {
    fun googleAuthWeb(code: String) = processOAuthLogin(googleService.exchangeCodeForUserInfoWeb(code), AuthType.GOOGLE)

    fun googleAuthApp(
        code: String,
        platform: String,
        codeVerifier: String,
    ) = processOAuthLogin(googleService.exchangeCodeForUserInfoApp(code, platform, codeVerifier), AuthType.GOOGLE)

    fun naverAuth(code: String) = processOAuthLogin(naverService.exchangeCodeForUserInfo(code), AuthType.NAVER)

    fun appleAuth(code: String) = processOAuthLogin(appleService.exchangeCodeForUserInfo(code), AuthType.APPLE)

    private fun processOAuthLogin(
        info: OAuthUserInfo,
        provider: AuthType,
    ): OAuthResponse {
        val existingUser = userRepository.findByEmail(info.email)
        val user = existingUser ?: userRepository.save(info.toUser(provider))
        val isFirstLogin = user.isFirstLogin
        if (isFirstLogin) {
            user.isFirstLogin = false
            userRepository.save(user)
        }
        return OAuthResponse(
            access = tokenProvider.generateAccess(user),
            refresh = tokenProvider.generateRefresh(user),
            isFirstLogin = isFirstLogin,
        )
    }
}
