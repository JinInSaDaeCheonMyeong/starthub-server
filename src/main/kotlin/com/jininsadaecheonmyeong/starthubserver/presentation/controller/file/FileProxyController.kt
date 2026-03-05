package com.jininsadaecheonmyeong.starthubserver.presentation.controller.file

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/files")
class FileProxyController {
    @GetMapping("/{fileName}")
    fun proxyFile(
        @PathVariable fileName: String,
        @RequestParam url: String,
    ): ResponseEntity<ByteArray> {
        val fileBytes = URI(url).toURL().openStream().use { it.readBytes() }

        val contentType =
            when (fileName.substringAfterLast(".").lowercase()) {
                "pdf" -> MediaType.APPLICATION_PDF
                "hwp" -> MediaType.parseMediaType("application/x-hwp")
                "hwpx" -> MediaType.parseMediaType("application/vnd.hancom.hwpx")
                "doc" -> MediaType.parseMediaType("application/msword")
                "docx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                "xls" -> MediaType.parseMediaType("application/vnd.ms-excel")
                "xlsx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                else -> MediaType.APPLICATION_OCTET_STREAM
            }

        val encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20")

        return ResponseEntity.ok()
            .contentType(contentType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''$encodedFileName")
            .body(fileBytes)
    }
}
