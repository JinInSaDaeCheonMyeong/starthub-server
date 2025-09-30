package com.jininsadaecheonmyeong.starthubserver.global.security.config

import com.jininsadaecheonmyeong.starthubserver.domain.oauth2.handler.OAuth2FailureHandler
import com.jininsadaecheonmyeong.starthubserver.domain.oauth2.handler.OAuth2SuccessHandler
import com.jininsadaecheonmyeong.starthubserver.domain.oauth2.service.CustomOAuth2UserService
import com.jininsadaecheonmyeong.starthubserver.global.security.filter.PlatformAuthenticationFilter
import com.jininsadaecheonmyeong.starthubserver.global.security.filter.TokenExceptionFilter
import com.jininsadaecheonmyeong.starthubserver.global.security.oauth2.CookieOAuth2AuthorizationRequestRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.oauth2.CustomOAuth2AccessTokenResponseClient
import com.jininsadaecheonmyeong.starthubserver.global.security.oauth2.CustomOAuth2AuthorizationRequestResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val platformAuthenticationFilter: PlatformAuthenticationFilter,
    private val tokenExceptionFilter: TokenExceptionFilter,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val oAuth2FailureHandler: OAuth2FailureHandler,
    private val customOAuth2AccessTokenResponseClient: CustomOAuth2AccessTokenResponseClient,
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val cookieOAuth2AuthorizationRequestRepository: CookieOAuth2AuthorizationRequestRepository,
) {
    @Bean
    fun customOAuth2AuthorizationRequestResolver(): CustomOAuth2AuthorizationRequestResolver {
        return CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository)
    }

    @Bean
    @Order(1)
    fun oauth2FilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .securityMatcher("/login/oauth2/**", "/oauth2/**")
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .oauth2Login {
                it.authorizationEndpoint {
                        auth ->
                    auth.authorizationRequestResolver(customOAuth2AuthorizationRequestResolver())
                    auth.authorizationRequestRepository(cookieOAuth2AuthorizationRequestRepository)
                }
                it.userInfoEndpoint { u -> u.userService(customOAuth2UserService) }
                it.tokenEndpoint { t -> t.accessTokenResponseClient(customOAuth2AccessTokenResponseClient) }
                it.successHandler(oAuth2SuccessHandler)
                it.failureHandler(oAuth2FailureHandler)
            }
            .build()
    }

    @Bean
    @Order(2)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .securityMatcher { request ->
                !request.requestURI.startsWith("/swagger-ui") &&
                    !request.requestURI.startsWith("/v3/api-docs") &&
                    !request.requestURI.startsWith("/login/oauth2") &&
                    !request.requestURI.startsWith("/oauth2")
            }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests { request ->
                request
                    .requestMatchers("/user/**").permitAll()
                    .requestMatchers("/oauth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/announcements/**").permitAll()
                    .anyRequest().permitAll() // TODO Remove it
            }
            .addFilterAfter(platformAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(tokenExceptionFilter, PlatformAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.allowedOriginPatterns = listOf("*")
        corsConfiguration.allowedHeaders = listOf("*")
        corsConfiguration.allowedMethods = listOf("*")
        corsConfiguration.allowCredentials = true
        corsConfiguration.maxAge = 3600

        val urlBasedCorsConfigurationSource = UrlBasedCorsConfigurationSource()
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration)

        return urlBasedCorsConfigurationSource
    }
}
