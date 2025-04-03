package com.jininsadaecheonmyeong.starthubserver.global.security.configuration


import com.jininsadaecheonmyeong.starthubserver.global.security.filter.TokenExceptionFilter
import com.jininsadaecheonmyeong.starthubserver.global.security.filter.TokenFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val tokenFilter: TokenFilter,
    private val tokenExceptionFilter: TokenExceptionFilter
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .formLogin{ it.disable() }
            .httpBasic{ it.disable() }
            .cors { it.disable() }
            .authorizeHttpRequests{ request ->
                request
                    .requestMatchers("/auth/*").permitAll()
                    .requestMatchers("/user/*").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterAfter(tokenFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(tokenExceptionFilter, TokenFilter::class.java)
        return http.build()
    }

    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }
}

