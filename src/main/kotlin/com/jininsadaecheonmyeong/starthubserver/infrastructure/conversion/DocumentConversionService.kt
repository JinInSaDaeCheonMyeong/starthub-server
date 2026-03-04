package com.jininsadaecheonmyeong.starthubserver.infrastructure.conversion

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class DocumentConversionService(
    @param:Value("\${document.conversion.temp-dir:/tmp/document-conversion}")
    private val tempDir: String,
    @param:Value("\${document.conversion.timeout:120}")
    private val timeoutSeconds: Long,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        val CONVERTIBLE_EXTENSIONS = setOf("hwp", "hwpx", "doc", "docx", "xls", "xlsx", "ppt", "pptx")
    }

    fun convertToPdf(
        inputBytes: ByteArray,
        originalFileName: String,
    ): ByteArray? {
        val extension = originalFileName.substringAfterLast(".", "").lowercase()

        if (extension == "pdf") {
            return inputBytes
        }

        if (extension !in CONVERTIBLE_EXTENSIONS) {
            logger.warn("지원하지 않는 파일 형식: {}", extension)
            return null
        }

        val workDir = Path.of(tempDir, UUID.randomUUID().toString())
        Files.createDirectories(workDir)

        try {
            val inputFile = workDir.resolve(originalFileName).toFile()
            inputFile.writeBytes(inputBytes)

            val process =
                ProcessBuilder(
                    "libreoffice",
                    "--headless",
                    "--convert-to",
                    "pdf",
                    "--outdir",
                    workDir.toString(),
                    inputFile.absolutePath,
                ).apply {
                    redirectErrorStream(true)
                }.start()

            val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)

            if (!completed) {
                process.destroyForcibly()
                logger.error("문서 변환 시간 초과: {}", originalFileName)
                return null
            }

            if (process.exitValue() != 0) {
                val errorOutput = process.inputStream.bufferedReader().readText()
                logger.error("문서 변환 실패: {}, 오류: {}", originalFileName, errorOutput)
                return null
            }

            val pdfFileName = originalFileName.substringBeforeLast(".") + ".pdf"
            val pdfFile = workDir.resolve(pdfFileName).toFile()

            if (!pdfFile.exists()) {
                logger.error("변환된 PDF 파일을 찾을 수 없음: {}", pdfFileName)
                return null
            }

            return pdfFile.readBytes()
        } finally {
            workDir.toFile().deleteRecursively()
        }
    }

    fun isConvertible(fileName: String): Boolean {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return extension in CONVERTIBLE_EXTENSIONS || extension == "pdf"
    }
}
