package com.jininsadaecheonmyeong.starthubserver.domain.oauth.service

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthType
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.service.AppleService
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthResponse
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthUserInfo
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.service.GoogleService
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.service.NaverService
import com.jininsadaecheonmyeong.starthubserver.global.security.token.service.TokenService
import org.springframework.stereotype.Service

@Service
class OAuthService(
    private val googleService: GoogleService,
    private val naverService: NaverService,
    private val appleService: AppleService,
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
) {
    fun googleAuthApp(
        code: String,
        platform: String,
        codeVerifier: String,
    ) = processOAuthLogin(googleService.exchangeCodeForUserInfoApp(code, platform, codeVerifier), AuthType.GOOGLE)

    fun naverAuthApp(code: String) = processOAuthLogin(naverService.exchangeCodeForUserInfoApp(code), AuthType.NAVER)

    fun appleAuthApp(idToken: String) = processOAuthLogin(appleService.exchangeCodeForUserInfoApp(idToken), AuthType.APPLE)

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
            access = tokenService.generateAccess(user),
            refresh = tokenService.generateAndStoreRefreshToken(user),
            isFirstLogin = isFirstLogin,
        )
    }
}
