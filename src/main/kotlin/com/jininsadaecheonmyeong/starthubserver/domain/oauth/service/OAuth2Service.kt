package com.jininsadaecheonmyeong.starthubserver.domain.oauth.service

import com.jininsadaecheonmyeong.starthubserver.domain.user.data.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enumeration.AuthProvider
import com.jininsadaecheonmyeong.starthubserver.domain.user.enumeration.UserRole
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthUserInfo
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.service.GoogleService
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.service.NaverService
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OAuth2Service(
    private val tokenProvider: TokenProvider,
    private val googleService: GoogleService,
    private val naverService: NaverService,
    private val userRepository: UserRepository
) {

    fun googleAuth(code: String, authProvider: AuthProvider): TokenResponse {
        val userInfo = googleService.exchangeCodeForUserInfo(code)
        return processOAuthLogin(userInfo, authProvider)
    }

    fun naverAuth(code: String, state: String, authProvider: AuthProvider): TokenResponse {
        val userInfo = naverService.exchangeCodeForUserInfo(code, state)
        return processOAuthLogin(userInfo, authProvider)
    }

    private fun processOAuthLogin(oAuthUserInfo: OAuthUserInfo, authProvider: AuthProvider): TokenResponse {
        if (!userRepository.existsByEmail(oAuthUserInfo.email)) {
            val user = User(
                email = oAuthUserInfo.email,
                role = UserRole.USER,
                provider = authProvider,
                providerId = oAuthUserInfo.id
            )
            userRepository.save(user)
            return TokenResponse(
                tokenProvider.generateAccess(user),
                tokenProvider.generateRefresh(user)
            )
        }

        val user = userRepository.findByEmail(oAuthUserInfo.email) ?: throw UserNotFoundException("유저를 찾을 수 없음")
        return TokenResponse(
            tokenProvider.generateAccess(user),
            tokenProvider.generateRefresh(user)
        )
    }
}