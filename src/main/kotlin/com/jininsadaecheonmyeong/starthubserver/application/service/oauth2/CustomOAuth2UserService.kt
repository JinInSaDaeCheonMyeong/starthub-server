package com.jininsadaecheonmyeong.starthubserver.application.service.oauth2

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.AuthType
import com.jininsadaecheonmyeong.starthubserver.domain.repository.user.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.parser.AppleTokenParser
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.oauth2.CustomOAuth2User
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val appleTokenParser: AppleTokenParser,
) : DefaultOAuth2UserService() {
    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val provider = AuthType.valueOf(userRequest.clientRegistration.registrationId.uppercase())

        val oAuth2User: OAuth2User =
            if (provider == AuthType.APPLE) {
                val idToken = userRequest.additionalParameters["id_token"] as String
                val attributes = appleTokenParser.parseIdToken(idToken)
                DefaultOAuth2User(
                    listOf(SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "sub",
                )
            } else {
                super.loadUser(userRequest)
            }

        val userInfo = provider.extractUserInfo(oAuth2User.attributes)

        var isFirstLogin = false
        val user: User =
            userRepository.findByProviderAndProviderId(provider, userInfo.providerId)
                ?: userRepository.findByEmail(userInfo.email)
                ?: run {
                    isFirstLogin = true
                    userRepository.save(userInfo.toEntity(provider))
                }

        return CustomOAuth2User(
            oAuth2User = oAuth2User,
            user = user,
            isFirstLogin = isFirstLogin,
        )
    }
}
