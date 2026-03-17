package com.jininsadaecheonmyeong.starthubserver.application.usecase.aichatbot

import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.AnnouncementResult
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.ChatbotRAGClient
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.ClaudeAIService
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.DocumentChunk
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.EmbedDocumentRequest
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.QueryContextRequest
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.QueryDocumentRequest
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.ReferenceParser
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.StreamChunk
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.StreamEventType
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.UserContextService
import com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot.AIChatDocument
import com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot.AIChatMessage
import com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot.AIChatSession
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.enums.aichatbot.MessageRole
import com.jininsadaecheonmyeong.starthubserver.domain.exception.aichatbot.ChatSessionNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.aichatbot.UnauthorizedChatAccessException
import com.jininsadaecheonmyeong.starthubserver.domain.repository.aichatbot.AIChatDocumentRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.aichatbot.AIChatMessageRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.aichatbot.AIChatSessionRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.announcement.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.global.infra.ai.ClaudePromptTemplates
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot.ChatSessionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@Transactional(readOnly = true)
class AIChatbotUseCase(
    private val sessionRepository: AIChatSessionRepository,
    private val messageRepository: AIChatMessageRepository,
    private val documentRepository: AIChatDocumentRepository,
    private val claudeAIService: ClaudeAIService,
    private val chatbotRAGClient: ChatbotRAGClient,
    private val userContextService: UserContextService,
    private val userAuthenticationHolder: UserAuthenticationHolder,
    private val referenceParser: ReferenceParser,
    private val announcementRepository: AnnouncementRepository,
) {
    @Transactional
    fun createSession(title: String?): AIChatSession {
        val user = userAuthenticationHolder.current()
        val session =
            AIChatSession(
                user = user,
                title = title ?: "New Chat",
            )
        return sessionRepository.save(session)
    }

    @Transactional
    fun getSessions(): List<ChatSessionResponse> {
        val user = userAuthenticationHolder.current()
        val sessions = sessionRepository.findByUserWithCollections(user)

        val cutoff = LocalDateTime.now().minusMinutes(5)
        val (emptySessions, activeSessions) = sessions.partition { it.messages.isEmpty() }
        val staleEmptySessions = emptySessions.filter { it.createdAt.isBefore(cutoff) }
        if (staleEmptySessions.isNotEmpty()) {
            staleEmptySessions.forEach { it.delete() }
            sessionRepository.saveAll(staleEmptySessions)
        }

        val remainingSessions = activeSessions + emptySessions.filter { !it.createdAt.isBefore(cutoff) }
        return remainingSessions.sortedByDescending { it.updatedAt }.map { ChatSessionResponse.from(it) }
    }

    fun getSession(sessionId: Long): AIChatSession {
        val user = userAuthenticationHolder.current()
        val session = findSessionOrThrow(sessionId)
        verifyOwnership(session, user)
        return session
    }

    fun getSessionWithMessages(sessionId: Long): AIChatSession {
        val user = userAuthenticationHolder.current()
        val session =
            sessionRepository.findByIdWithCollections(sessionId)
                ?: throw ChatSessionNotFoundException("채팅 세션을 찾을 수 없습니다.")
        verifyOwnership(session, user)
        return session
    }

    @Transactional
    fun updateSessionTitle(
        sessionId: Long,
        title: String,
    ): AIChatSession {
        val user = userAuthenticationHolder.current()
        val session = findSessionOrThrow(sessionId)
        verifyOwnership(session, user)
        session.updateTitle(title)
        return sessionRepository.save(session)
    }

    @Transactional
    fun deleteSession(sessionId: Long) {
        val user = userAuthenticationHolder.current()
        val session = findSessionOrThrow(sessionId)
        verifyOwnership(session, user)
        session.delete()
        sessionRepository.save(session)
    }

    @Transactional
    suspend fun processMessageStream(
        sessionId: Long,
        message: String,
        user: User,
        files: List<ProcessedFile>? = null,
    ): Flow<StreamChunk> {
        val session = findSessionOrThrow(sessionId)
        verifyOwnership(session, user)

        files?.forEach { file ->
            addDocument(
                sessionId = sessionId,
                fileName = file.fileName,
                fileUrl = file.fileUrl,
                fileType = file.fileType,
                extractedText = file.extractedText,
                user = user,
            )
        }

        saveUserMessage(session, message)

        val isFirstMessage = getAllMessages(sessionId).size == 1
        if (isFirstMessage) {
            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                try {
                    val title = claudeAIService.generateTitle(message)
                    session.updateTitle(title)
                    sessionRepository.save(session)
                } catch (_: Exception) {
                }
            }
        }

        val history = getRecentMessagesForHistory(sessionId, 20)
        val (userContextString, retrievedContext) =
            coroutineScope {
                val userCtxDeferred =
                    async(Dispatchers.IO) {
                        userContextService.buildContextStringWithAnalysis(user)
                    }
                val retrievedCtxDeferred =
                    async {
                        buildRetrievedContext(session, user, message)
                    }
                userCtxDeferred.await() to retrievedCtxDeferred.await()
            }

        val systemPrompt = ClaudePromptTemplates.buildStartupAssistantPrompt(userContextString)

        val imageAttachments = files?.filter { it.isImage() }?.mapNotNull { ImageAttachment.fromProcessedFile(it) }
        val enableWebSearch = shouldPerformWebSearch(message)

        val responseBuilder = StringBuilder()

        return claudeAIService.streamChat(
            systemPrompt = systemPrompt,
            history = history.dropLast(1),
            userMessage = message,
            retrievedContext = retrievedContext,
            imageAttachments = imageAttachments?.ifEmpty { null },
            enableWebSearch = enableWebSearch,
        ).map { chunk ->
            chunk.text?.let { responseBuilder.append(it) }
            chunk
        }.onCompletion { error ->
            if (error == null) {
                val fullResponse = responseBuilder.toString()
                if (fullResponse.isNotBlank()) {
                    saveAssistantMessage(session, fullResponse)

                    val parseResult = referenceParser.parseAndClean(fullResponse)
                    if (parseResult.references.isNotEmpty()) {
                        emit(
                            StreamChunk(
                                type = StreamEventType.MESSAGE_STOP,
                                references = parseResult.references,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun getDocuments(sessionId: Long): List<AIChatDocument> {
        val user = userAuthenticationHolder.current()
        val session = findSessionOrThrow(sessionId)
        verifyOwnership(session, user)
        return documentRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
    }

    @Transactional
    suspend fun addDocument(
        sessionId: Long,
        fileName: String,
        fileUrl: String,
        fileType: String,
        extractedText: String?,
        user: User,
    ): AIChatDocument {
        val session = findSessionOrThrow(sessionId)
        verifyOwnership(session, user)

        val document =
            AIChatDocument(
                session = session,
                fileName = fileName,
                fileUrl = fileUrl,
                fileType = fileType,
                extractedText = extractedText,
            )
        val savedDocument =
            withContext(Dispatchers.IO) {
                documentRepository.save(document)
            }

        if (!extractedText.isNullOrBlank()) {
            try {
                val chunks = chunkText(extractedText)
                chatbotRAGClient.embedDocument(
                    EmbedDocumentRequest(
                        documentId = savedDocument.id.toString(),
                        sessionId = sessionId,
                        chunks =
                            chunks.mapIndexed { index, content ->
                                DocumentChunk(index, content)
                            },
                    ),
                )
            } catch (_: Exception) {
            }
        }

        return savedDocument
    }

    @Transactional
    suspend fun deleteDocument(
        sessionId: Long,
        documentId: Long,
    ) {
        val user = userAuthenticationHolder.current()
        val session = findSessionOrThrow(sessionId)
        verifyOwnership(session, user)

        try {
            chatbotRAGClient.deleteDocument(documentId.toString())
        } catch (_: Exception) {
        }

        withContext(Dispatchers.IO) {
            documentRepository.deleteById(documentId)
        }
    }

    private fun findSessionOrThrow(sessionId: Long): AIChatSession {
        return sessionRepository.findByIdAndDeletedFalse(sessionId)
            ?: throw ChatSessionNotFoundException("채팅 세션을 찾을 수 없습니다.")
    }

    private fun verifyOwnership(
        session: AIChatSession,
        user: User,
    ) {
        if (!session.isOwner(user)) {
            throw UnauthorizedChatAccessException("접근 권한이 없습니다.")
        }
    }

    private fun saveUserMessage(
        session: AIChatSession,
        content: String,
    ): AIChatMessage {
        val message =
            AIChatMessage(
                session = session,
                role = MessageRole.USER,
                content = content,
            )
        return messageRepository.save(message)
    }

    private fun saveAssistantMessage(
        session: AIChatSession,
        content: String,
    ): AIChatMessage {
        val message =
            AIChatMessage(
                session = session,
                role = MessageRole.ASSISTANT,
                content = content,
            )
        return messageRepository.save(message)
    }

    private fun getAllMessages(sessionId: Long): List<AIChatMessage> {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
    }

    private fun getRecentMessagesForHistory(
        sessionId: Long,
        limit: Int,
    ): List<ChatHistoryMessage> {
        val messages = messageRepository.findRecentMessages(sessionId, PageRequest.of(0, limit)).reversed()
        return messages.map {
            ChatHistoryMessage(role = it.role, content = it.content)
        }
    }

    private suspend fun buildRetrievedContext(
        session: AIChatSession,
        user: User,
        message: String,
    ): String? =
        coroutineScope {
            val needRAGAnnouncements = isAnnouncementRelated(message)

            val documentDeferred =
                async {
                    val documents =
                        withContext(Dispatchers.IO) {
                            documentRepository.findBySessionIdOrderByCreatedAtAsc(session.id!!)
                        }
                    if (documents.isNotEmpty()) buildDocumentContext(documents, session.id!!, message) else null
                }

            val ragDeferred =
                async {
                    if (needRAGAnnouncements) {
                        queryRAGContext(user.id!!, message)
                    } else {
                        null
                    }
                }

            val contextParts = mutableListOf<String>()
            documentDeferred.await()?.let { contextParts.add(it) }
            ragDeferred.await()?.let { contextParts.add(it) }

            if (contextParts.isNotEmpty()) {
                contextParts.joinToString("\n\n---\n\n")
            } else {
                null
            }
        }

    private suspend fun buildDocumentContext(
        documents: List<AIChatDocument>,
        sessionId: Long,
        query: String,
    ): String? {
        val textDocuments = documents.filter { !it.isImage() }
        if (textDocuments.isEmpty()) return null

        return try {
            val ragResponse =
                chatbotRAGClient.queryDocument(
                    QueryDocumentRequest(
                        sessionId = sessionId,
                        query = query,
                        topK = 5,
                    ),
                )

            if (ragResponse == null || ragResponse.results.isEmpty()) {
                return buildFallbackDocumentContext(textDocuments)
            }

            val resultsByDocument = ragResponse.results.groupBy { it.documentId }

            buildString {
                appendLine("## 업로드된 문서 내용 (관련 부분)")
                resultsByDocument.forEach { (docId, chunks) ->
                    val document = textDocuments.find { it.id.toString() == docId }
                    val fileName = document?.fileName ?: "문서 $docId"

                    appendLine("### [문서]$fileName")
                    chunks.sortedBy { it.chunkIndex }.forEach { chunk ->
                        appendLine("(관련도: ${String.format("%.2f", chunk.score)})")
                        appendLine(chunk.content)
                        appendLine()
                    }
                }
            }
        } catch (_: Exception) {
            buildFallbackDocumentContext(textDocuments)
        }
    }

    private fun buildFallbackDocumentContext(documents: List<AIChatDocument>): String? {
        val localContext =
            documents
                .filter { it.extractedText != null }
                .take(3)
                .joinToString("\n\n") { doc ->
                    "[문서]**${doc.fileName}**:\n${doc.extractedText!!.take(2000)}"
                }

        if (localContext.isBlank()) return null

        return buildString {
            appendLine("## 업로드된 문서 내용")
            append(localContext)
        }
    }

    private fun shouldPerformWebSearch(message: String): Boolean {
        return ClaudePromptTemplates.WEB_SEARCH_KEYWORDS.any { keyword ->
            message.contains(keyword, ignoreCase = true)
        }
    }

    private fun isAnnouncementRelated(message: String): Boolean {
        return ClaudePromptTemplates.ANNOUNCEMENT_KEYWORDS.any { keyword ->
            message.contains(keyword, ignoreCase = true)
        }
    }

    private suspend fun queryRAGContext(
        userId: Long,
        query: String,
    ): String? {
        return try {
            val response =
                chatbotRAGClient.queryContext(
                    QueryContextRequest(
                        query = query,
                        userId = null,
                        topK = 5,
                        includeAnnouncements = true,
                    ),
                ) ?: return null

            buildAnnouncementContextString(response.announcements)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun buildAnnouncementContextString(announcements: List<AnnouncementResult>?): String? {
        if (announcements.isNullOrEmpty()) return null

        val urls = announcements.map { it.url }
        val dbAnnouncements =
            withContext(Dispatchers.IO) {
                announcementRepository.findByUrlIn(urls)
            }
        val urlToDbAnnouncement = dbAnnouncements.associateBy { it.url }

        val today = java.time.LocalDate.now()

        return buildString {
            appendLine("## 관련 지원 공고")
            announcements.take(5).forEach { announcement ->
                val dbAnnouncement = urlToDbAnnouncement[announcement.url]
                val isExpired = dbAnnouncement?.let { isAnnouncementExpired(it.receptionPeriod, today) } ?: false

                if (isExpired) {
                    appendLine("### ${announcement.title} (접수 마감)")
                } else {
                    appendLine("### ${announcement.title}")
                }
                appendLine("- 기관: ${announcement.organization ?: "정보 없음"}")
                if (dbAnnouncement != null && !isExpired) {
                    appendLine("- [StartHub 내부 공고] ID: ${dbAnnouncement.id}, URL: ${dbAnnouncement.url}")
                    appendLine("- 접수기간: ${dbAnnouncement.receptionPeriod}")
                    val ref =
                        "[[ANNOUNCEMENT:${dbAnnouncement.id}" +
                            ":${announcement.title}:${dbAnnouncement.url}]]"
                    appendLine("- 반드시 $ref 형식으로 참조하십시오.")
                    appendLine("- 외부 링크를 직접 노출하지 마십시오.")
                } else if (dbAnnouncement == null) {
                    appendLine("- [외부 공고] 링크: ${announcement.url}")
                    appendLine("- StartHub에 등록되지 않은 외부 공고입니다. 일반 마크다운 링크로 안내하십시오.")
                }
                if (isExpired) {
                    appendLine("- 접수 마감된 공고입니다. 추천하지 마십시오.")
                }
                appendLine()
            }
            appendLine()
            appendLine("중요 규칙:")
            appendLine("- StartHub 내부 공고는 반드시 [[ANNOUNCEMENT:ID:제목:URL]] 형식만 사용하십시오. 외부 링크(https://...)를 직접 텍스트로 노출하지 마십시오.")
            appendLine("- 외부 공고만 일반 마크다운 링크 [제목](URL) 형식을 사용하십시오.")
        }
    }

    private fun isAnnouncementExpired(
        receptionPeriod: String,
        today: java.time.LocalDate,
    ): Boolean {
        return try {
            val endDateStr = receptionPeriod.split("~").last().trim()
            val endDate =
                java.time.LocalDateTime.parse(
                    endDateStr,
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                )
            today.isAfter(endDate.toLocalDate())
        } catch (_: Exception) {
            false
        }
    }

    private fun chunkText(
        text: String,
        chunkSize: Int = 1000,
        overlap: Int = 200,
    ): List<String> {
        val chunks = mutableListOf<String>()
        var start = 0

        while (start < text.length) {
            val end = minOf(start + chunkSize, text.length)
            chunks.add(text.substring(start, end))
            start += chunkSize - overlap
        }

        return chunks
    }
}

data class ChatHistoryMessage(
    val role: MessageRole,
    val content: String,
)

data class ProcessedFile(
    val fileName: String,
    val fileUrl: String,
    val fileType: String,
    val extractedText: String?,
) {
    fun isImage(): Boolean = fileType.lowercase() in listOf("png", "jpg", "jpeg", "gif", "webp")
}

data class ImageAttachment(
    val fileName: String,
    val fileUrl: String,
    val mediaType: String,
) {
    companion object {
        private val MEDIA_TYPE_MAP =
            mapOf(
                "png" to "image/png",
                "jpg" to "image/jpeg",
                "jpeg" to "image/jpeg",
                "gif" to "image/gif",
                "webp" to "image/webp",
            )

        fun fromProcessedFile(file: ProcessedFile): ImageAttachment? {
            val mediaType = MEDIA_TYPE_MAP[file.fileType.lowercase()] ?: return null
            return ImageAttachment(
                fileName = file.fileName,
                fileUrl = file.fileUrl,
                mediaType = mediaType,
            )
        }
    }
}
