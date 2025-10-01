package com.jininsadaecheonmyeong.starthubserver.global.security.oauth2

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest

class CustomOAuth2AuthorizationRequestResolver(
    private val clientRegistrationRepository: ClientRegistrationRepository,
) : OAuth2AuthorizationRequestResolver {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val defaultResolver = DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization")

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val authorizationRequest = defaultResolver.resolve(request)
        val registrationId = authorizationRequest?.getAttribute("registration_id") as String?
        return if (authorizationRequest != null && registrationId == "apple") {
            customizeAppleRequest(authorizationRequest)
        } else {
            authorizationRequest
        }
    }

    override fun resolve(
        request: HttpServletRequest,
        clientRegistrationId: String,
    ): OAuth2AuthorizationRequest? {
        val authorizationRequest = defaultResolver.resolve(request, clientRegistrationId)
        return if (authorizationRequest != null && clientRegistrationId == "apple") {
            customizeAppleRequest(authorizationRequest)
        } else {
            authorizationRequest
        }
    }

    private fun customizeAppleRequest(req: OAuth2AuthorizationRequest): OAuth2AuthorizationRequest {
        log.info("Apple OAuth2 redirect_uri: {}", req.redirectUri)

        val additionalParameters = mutableMapOf<String, Any>()
        additionalParameters.putAll(req.additionalParameters)
        additionalParameters["response_mode"] = "form_post"

        return OAuth2AuthorizationRequest.from(req)
            .additionalParameters(additionalParameters)
            .build()
    }
}
