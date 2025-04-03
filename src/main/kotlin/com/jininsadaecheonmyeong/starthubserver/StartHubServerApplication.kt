package com.jininsadaecheonmyeong.starthubserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StartHubServerApplication

fun main(args: Array<String>) {
    runApplication<StartHubServerApplication>(*args)
}
