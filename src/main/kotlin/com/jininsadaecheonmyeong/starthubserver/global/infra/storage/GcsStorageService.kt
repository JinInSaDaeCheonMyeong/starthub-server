package com.jininsadaecheonmyeong.starthubserver.global.infra.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class GcsStorageService(
    private val storage: Storage,
) {
    @Value("\${gcs.bucket-name}")
    private lateinit var bucketName: String

    fun uploadFile(
        file: MultipartFile,
        directory: String,
    ): String {
        val fileName = generateFileName(file.originalFilename ?: "file")
        val objectName = "$directory/$fileName"

        val blobId = BlobId.of(bucketName, objectName)
        val blobInfo =
            BlobInfo
                .newBuilder(blobId)
                .setContentType(file.contentType)
                .build()

        storage.create(blobInfo, file.bytes)

        return "https://storage.googleapis.com/$bucketName/$objectName"
    }

    fun deleteFile(fileUrl: String) {
        val objectName = extractObjectNameFromUrl(fileUrl)
        val blobId = BlobId.of(bucketName, objectName)
        storage.delete(blobId)
    }

    fun uploadBytes(
        bytes: ByteArray,
        fileName: String,
        directory: String,
        contentType: String,
    ): String {
        val generatedFileName = generateFileName(fileName)
        val objectName = "$directory/$generatedFileName"

        val blobId = BlobId.of(bucketName, objectName)
        val blobInfo =
            BlobInfo
                .newBuilder(blobId)
                .setContentType(contentType)
                .build()

        storage.create(blobInfo, bytes)

        return "https://storage.googleapis.com/$bucketName/$objectName"
    }

    private fun generateFileName(originalFilename: String): String {
        val extension = originalFilename.substringAfterLast(".", "")
        val uuid = UUID.randomUUID().toString()
        return if (extension.isNotEmpty()) "$uuid.$extension" else uuid
    }

    private fun extractObjectNameFromUrl(fileUrl: String): String {
        return fileUrl.substringAfter("$bucketName/")
    }
}
