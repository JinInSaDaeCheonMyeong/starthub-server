package com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot

import com.jininsadaecheonmyeong.starthubserver.logger
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileProcessingService {
    private val log = logger()

    fun extractTextFromPdf(file: MultipartFile): String? {
        return try {
            file.inputStream.use { inputStream ->
                val document = Loader.loadPDF(inputStream.readAllBytes())
                val stripper = PDFTextStripper()
                val text = stripper.getText(document)
                document.close()
                text.trim().takeIf { it.isNotBlank() }
            }
        } catch (e: Exception) {
            log.error("PDF 텍스트 추출 실패: ${e.message}")
            null
        }
    }

    fun extractTextFromDocx(file: MultipartFile): String? {
        return try {
            file.inputStream.use { inputStream ->
                val document = XWPFDocument(inputStream)
                val text =
                    buildString {
                        document.paragraphs.forEach { paragraph ->
                            appendLine(paragraph.text)
                        }

                        document.tables.forEach { table ->
                            table.rows.forEach { row ->
                                row.tableCells.forEach { cell ->
                                    append(cell.text)
                                    append("\t")
                                }
                                appendLine()
                            }
                        }
                    }
                document.close()
                text.trim().takeIf { it.isNotBlank() }
            }
        } catch (e: Exception) {
            log.error("DOCX 텍스트 추출 실패: ${e.message}")
            null
        }
    }

    fun extractTextFromFile(
        file: MultipartFile,
        fileType: String,
    ): String? {
        return when (fileType.lowercase()) {
            "pdf" -> extractTextFromPdf(file)
            "docx", "doc" -> extractTextFromDocx(file)
            else -> null
        }
    }
}
