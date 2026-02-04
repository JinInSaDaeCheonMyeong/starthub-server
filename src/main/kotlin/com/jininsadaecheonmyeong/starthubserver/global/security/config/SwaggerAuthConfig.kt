package com.jininsadaecheonmyeong.starthubserver.global.security.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@ConditionalOnProperty(name = ["swagger.auth.enabled"], havingValue = "true")
class SwaggerAuthConfig(
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${swagger.auth.username}")
    private val swaggerUsername: String,
    @param:Value("\${swagger.auth.password}")
    private val swaggerPassword: String,
) {
    @Bean
    @Order(1)
    fun swaggerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .securityMatcher("/swagger-ui/**", "/v3/api-docs/**")
            .authorizeHttpRequests { authz ->
                authz.anyRequest().authenticated()
            }
            .httpBasic { basic ->
                basic.realmName("Swagger Documentation")
            }
            .userDetailsService(swaggerUserDetailsService())
            .csrf { it.disable() }
            .build()
    }

    @Bean("swaggerUserDetailsService")
    fun swaggerUserDetailsService(): UserDetailsService {
        val admin: UserDetails =
            User.builder()
                .username(swaggerUsername)
                .password(passwordEncoder.encode(swaggerPassword))
                .roles("ADMIN")
                .build()

        return InMemoryUserDetailsManager(admin)
    }
}
