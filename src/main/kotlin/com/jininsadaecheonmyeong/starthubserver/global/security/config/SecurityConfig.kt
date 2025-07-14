package com.jininsadaecheonmyeong.starthubserver.global.security.config

import com.jininsadaecheonmyeong.starthubserver.global.security.filter.TokenExceptionFilter
import com.jininsadaecheonmyeong.starthubserver.global.security.filter.PlatformAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
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
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests { request ->
                request
                    .requestMatchers("/user/*").permitAll()
                    .requestMatchers("/oauth/*").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .anyRequest().permitAll() // TODO Remove it
            }
            .addFilterAfter(platformAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(tokenExceptionFilter, PlatformAuthenticationFilter::class.java)
        return http.build()
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
