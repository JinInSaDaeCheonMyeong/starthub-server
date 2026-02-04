package com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot

import com.jininsadaecheonmyeong.starthubserver.logger
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class ChatbotRAGClient(
    @param:Value("\${chatbot.rag-api-url}")
    private val ragApiUrl: String,
) {
    private val log = logger()

    private val webClient: WebClient by lazy {
        WebClient.builder()
            .baseUrl(ragApiUrl)
            .codecs { it.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) }
            .build()
    }

    suspend fun embedUserContext(request: EmbedUserContextRequest): Boolean {
        return try {
            webClient.post()
                .uri("/chatbot/embed-user-context")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono<Map<String, Any>>()
                .awaitSingleOrNull()
            true
        } catch (e: Exception) {
            log.error("사용자 컨텍스트 임베딩 실패: ${e.message}")
            false
        }
    }

    suspend fun queryContext(request: QueryContextRequest): QueryContextResponse? {
        return try {
            webClient.post()
                .uri("/chatbot/query-context")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono<QueryContextResponse>()
                .awaitSingleOrNull()
        } catch (e: Exception) {
            log.error("컨텍스트 쿼리 실패: ${e.message}")
            null
        }
    }

    suspend fun embedDocument(request: EmbedDocumentRequest): Boolean {
        return try {
            webClient.post()
                .uri("/chatbot/embed-document")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono<Map<String, Any>>()
                .awaitSingleOrNull()
            true
        } catch (e: Exception) {
            log.error("문서 임베딩 실패: ${e.message}")
            false
        }
    }

    suspend fun queryDocument(request: QueryDocumentRequest): QueryDocumentResponse? {
        return try {
            webClient.post()
                .uri("/chatbot/query-document")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono<QueryDocumentResponse>()
                .awaitSingleOrNull()
        } catch (e: Exception) {
            log.error("문서 쿼리 실패: ${e.message}")
            null
        }
    }

    suspend fun deleteUserContext(userId: Long): Boolean {
        return try {
            webClient.delete()
                .uri("/chatbot/user-context/$userId")
                .retrieve()
                .bodyToMono<Map<String, Any>>()
                .awaitSingleOrNull()
            true
        } catch (e: Exception) {
            log.error("사용자 컨텍스트 삭제 실패: ${e.message}")
            false
        }
    }

    suspend fun deleteDocument(documentId: String): Boolean {
        return try {
            webClient.delete()
                .uri("/chatbot/document/$documentId")
                .retrieve()
                .bodyToMono<Map<String, Any>>()
                .awaitSingleOrNull()
            true
        } catch (e: Exception) {
            log.error("문서 벡터 삭제 실패: ${e.message}")
            false
        }
    }
}

data class EmbedUserContextRequest(
    val userId: Long,
    val bmcs: List<BmcEmbedData>,
    val interests: List<String>,
    val likedAnnouncementUrls: List<String>,
    val competitorAnalyses: List<CompetitorAnalysisEmbedData>? = null,
)

data class BmcEmbedData(
    val id: Long,
    val title: String,
    val valueProposition: String?,
    val customerSegments: String?,
    val channels: String?,
    val customerRelationships: String?,
    val revenueStreams: String?,
    val keyResources: String?,
    val keyActivities: String?,
    val keyPartners: String?,
    val costStructure: String?,
)

data class CompetitorAnalysisEmbedData(
    val id: Long,
    val bmcId: Long,
    val bmcTitle: String,
    val userBmcSummary: String?,
    val strengths: String?,
    val weaknesses: String?,
    val globalStrategy: String?,
)

data class QueryContextRequest(
    val query: String,
    val userId: Long?,
    val topK: Int = 5,
    val includeAnnouncements: Boolean = true,
)

data class QueryContextResponse(
    val userContext: List<ContextResult>?,
    val announcements: List<AnnouncementResult>?,
)

data class ContextResult(
    val id: String,
    val content: String,
    val score: Double,
    val type: String,
)

data class AnnouncementResult(
    val id: String,
    val title: String,
    val url: String,
    val organization: String?,
    val score: Double,
)

data class EmbedDocumentRequest(
    val documentId: String,
    val sessionId: Long,
    val chunks: List<DocumentChunk>,
)

data class DocumentChunk(
    val index: Int,
    val content: String,
)

data class QueryDocumentRequest(
    val sessionId: Long,
    val query: String,
    val topK: Int = 3,
)

data class QueryDocumentResponse(
    val results: List<DocumentQueryResult>,
)

data class DocumentQueryResult(
    val documentId: String,
    val content: String,
    val score: Double,
    val chunkIndex: Int,
)
