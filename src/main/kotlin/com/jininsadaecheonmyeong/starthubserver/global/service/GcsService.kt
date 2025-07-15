package com.jininsadaecheonmyeong.starthubserver.global.service

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class GcsService(
    private val storage: Storage,
) {
    @Value("\${gcs.bucket-name}")
    private lateinit var bucketName: String

    private val allowedImageTypes = setOf("image/jpeg", "image/png", "image/gif", "image/webp")
    private val maxFileSize = 5 * 1024 * 1024 // 5MB

    fun uploadFile(
        file: MultipartFile,
        folder: String,
    ): String {
        validateFile(file)

        val fileName = generateFileName(file.originalFilename, folder)
        val blobId = com.google.cloud.storage.BlobId.of(bucketName, fileName)
        val blobInfo =
            BlobInfo.newBuilder(blobId)
                .setContentType(file.contentType)
                .build()

        try {
            storage.create(blobInfo, file.bytes)
        } catch (e: Exception) {
            throw RuntimeException("파일 업로드 중 오류가 발생했습니다: ${e.message}", e)
        }

        return "https://storage.googleapis.com/$bucketName/$fileName"
    }

    fun deleteFile(fileName: String): Boolean {
        val blobId = com.google.cloud.storage.BlobId.of(bucketName, fileName)
        return storage.delete(blobId)
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) throw IllegalArgumentException("업로드할 파일이 없습니다")
        if (file.size > maxFileSize) throw IllegalArgumentException("파일 크기가 5MB를 초과했습니다")
        if (file.contentType !in allowedImageTypes) throw IllegalArgumentException("지원하지 않는 파일 형식입니다. (지원 형식: JPG, PNG, GIF, WebP)")
    }

    private fun generateFileName(
        originalFileName: String?,
        folder: String,
    ): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString()
        val extension = originalFileName?.substringAfterLast('.', "") ?: "jpg"
        return "$folder/${timestamp}_$uuid.$extension"
    }
}
