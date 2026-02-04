package com.jininsadaecheonmyeong.starthubserver.application.usecase.aichatbot

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
import com.jininsadaecheonmyeong.starthubserver.global.infra.ai.ClaudePromptTemplates
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.PerplexitySearchService
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.model.SearchRequest
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot.ChatSessionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class AIChatbotUseCase(
    private val sessionRepository: AIChatSessionRepository,
    private val messageRepository: AIChatMessageRepository,
    private val documentRepository: AIChatDocumentRepository,
    private val claudeAIService: ClaudeAIService,
    private val chatbotRAGClient: ChatbotRAGClient,
    private val userContextService: UserContextService,
    private val perplexitySearchService: PerplexitySearchService,
    private val userAuthenticationHolder: UserAuthenticationHolder,
    private val referenceParser: ReferenceParser,
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

    fun getSessions(): List<ChatSessionResponse> {
        val user = userAuthenticationHolder.current()
        val sessions = sessionRepository.findByUserWithCollections(user)
        return sessions.map { ChatSessionResponse.from(it) }
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

        if (getAllMessages(sessionId).size == 1) {
            try {
                val title = claudeAIService.generateTitle(message)
                session.updateTitle(title)
                withContext(Dispatchers.IO) {
                    sessionRepository.save(session)
                }
            } catch (_: Exception) {
            }
        }

        val history = getRecentMessagesForHistory(sessionId, 20)
        val userContextString =
            withContext(Dispatchers.IO) {
                userContextService.buildContextStringWithAnalysis(user)
            }
        val retrievedContext = buildRetrievedContext(session, user, message)
        val systemPrompt = ClaudePromptTemplates.buildStartupAssistantPrompt(userContextString)

        val responseBuilder = StringBuilder()

        return claudeAIService.streamChat(
            systemPrompt = systemPrompt,
            history = history.dropLast(1),
            userMessage = message,
            retrievedContext = retrievedContext,
        ).map { chunk ->
            chunk.text?.let { responseBuilder.append(it) }
            chunk
        }.onCompletion { error ->
            if (error == null) {
                val fullResponse = responseBuilder.toString()
                if (fullResponse.isNotBlank()) {
                    val parseResult = referenceParser.parseAndClean(fullResponse)
                    saveAssistantMessage(session, parseResult.cleanedContent)

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
        val savedDocument = withContext(Dispatchers.IO) {
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
    ): String? {
        val contextParts = mutableListOf<String>()

        val documents =
            withContext(Dispatchers.IO) {
                documentRepository.findBySessionIdOrderByCreatedAtAsc(session.id!!)
            }
        if (documents.isNotEmpty()) {
            val documentContext = buildDocumentContext(documents, session.id!!, message)
            documentContext?.let { contextParts.add(it) }
        }

        if (shouldPerformWebSearch(message)) {
            val webSearchResult = performWebSearch(message)
            webSearchResult?.let { contextParts.add(it) }
        }

        if (isAnnouncementRelated(message)) {
            val announcementContext = queryAnnouncementContext(user.id!!, message)
            announcementContext?.let { contextParts.add(it) }
        }

        if (isUserContextRelated(message)) {
            val userRagContext = queryUserContext(user.id!!, message)
            userRagContext?.let { contextParts.add(it) }
        }

        return if (contextParts.isNotEmpty()) {
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

                    appendLine("### 📄 $fileName")
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
                    "📄 **${doc.fileName}**:\n${doc.extractedText!!.take(2000)}"
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

    private suspend fun performWebSearch(message: String): String? {
        return try {
            val results =
                perplexitySearchService.searchCompetitors(
                    SearchRequest(query = message, maxResults = 3),
                )

            if (results.isEmpty()) return null

            buildString {
                appendLine("## 웹 검색 결과")
                results.take(3).forEach { result ->
                    appendLine("### ${result.title}")
                    appendLine("URL: ${result.url}")
                    appendLine(result.snippet)
                    appendLine()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun isAnnouncementRelated(message: String): Boolean {
        return ClaudePromptTemplates.ANNOUNCEMENT_KEYWORDS.any { keyword ->
            message.contains(keyword, ignoreCase = true)
        }
    }

    private fun isUserContextRelated(message: String): Boolean {
        return ClaudePromptTemplates.USER_CONTEXT_KEYWORDS.any { keyword ->
            message.contains(keyword, ignoreCase = true)
        }
    }

    private suspend fun queryUserContext(
        userId: Long,
        query: String,
    ): String? {
        return try {
            val response =
                chatbotRAGClient.queryContext(
                    QueryContextRequest(
                        query = query,
                        userId = userId,
                        topK = 5,
                        includeAnnouncements = false,
                    ),
                )

            if (response?.userContext.isNullOrEmpty()) return null

            buildString {
                appendLine("## 사용자 관련 정보 (RAG 검색 결과)")
                response!!.userContext!!.forEach { result ->
                    when (result.type) {
                        "bmc" -> appendLine("### BMC 관련 (관련도: ${String.format("%.2f", result.score)})")
                        "interests" -> appendLine("### 관심분야 (관련도: ${String.format("%.2f", result.score)})")
                        "competitor_analysis" -> appendLine("### 경쟁사분석 (관련도: ${String.format("%.2f", result.score)})")
                        "liked_preference" -> appendLine("### 관심 공고 기반 (관련도: ${String.format("%.2f", result.score)})")
                        else -> appendLine("### 기타 (관련도: ${String.format("%.2f", result.score)})")
                    }
                    appendLine(result.content)
                    appendLine()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun queryAnnouncementContext(
        userId: Long,
        query: String,
    ): String? {
        return try {
            val response =
                chatbotRAGClient.queryContext(
                    QueryContextRequest(
                        query = query,
                        userId = userId,
                        topK = 5,
                        includeAnnouncements = true,
                    ),
                )

            if (response?.announcements.isNullOrEmpty()) return null

            buildString {
                appendLine("## 관련 지원 공고")
                response!!.announcements!!.take(5).forEach { announcement ->
                    appendLine("### ${announcement.title}")
                    appendLine("- 기관: ${announcement.organization ?: "정보 없음"}")
                    appendLine("- 링크: ${announcement.url}")
                    appendLine()
                }
            }
        } catch (_: Exception) {
            null
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
)
