package com.jininsadaecheonmyeong.starthubserver.presentation.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf("status" to "ok", "version" to "2026-05-06-deploy-test")
}