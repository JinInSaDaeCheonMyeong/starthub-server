package com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot

import org.springframework.stereotype.Component

@Component
class ReferenceParser {
    companion object {
        private val BMC_PATTERN = Regex("""\[\[BMC:(\d+):([^]]+)]]""")
        private val ANALYSIS_PATTERN = Regex("""\[\[ANALYSIS:(\d+):([^]]+)]]""")
        private val ANNOUNCEMENT_PATTERN = Regex("""\[\[ANNOUNCEMENT:(\d+):([^:]+):([^]]+)]]""")
        private val SCHEDULE_PATTERN = Regex("""\[\[SCHEDULE:(\d+):([^:]+):([^]]+)]]""")
    }

    fun parseAndClean(content: String): ParseResult {
        val references = mutableListOf<Reference>()
        var cleanedContent = content

        BMC_PATTERN.findAll(content).forEach { match ->
            val id = match.groupValues[1].toLongOrNull()
            val title = match.groupValues[2]
            if (id != null) {
                references.add(Reference(type = ReferenceType.BMC, id = id, title = title, url = null))
            }
        }
        cleanedContent = BMC_PATTERN.replace(cleanedContent) { "**${it.groupValues[2]}**" }

        ANALYSIS_PATTERN.findAll(content).forEach { match ->
            val id = match.groupValues[1].toLongOrNull()
            val title = match.groupValues[2]
            if (id != null) {
                references.add(Reference(type = ReferenceType.ANALYSIS, id = id, title = title, url = null))
            }
        }
        cleanedContent = ANALYSIS_PATTERN.replace(cleanedContent) { "**${it.groupValues[2]}**" }

        ANNOUNCEMENT_PATTERN.findAll(content).forEach { match ->
            val id = match.groupValues[1].toLongOrNull()
            val title = match.groupValues[2]
            val url = match.groupValues[3]
            if (id != null) {
                references.add(Reference(type = ReferenceType.ANNOUNCEMENT, id = id, title = title, url = url))
            }
        }
        cleanedContent = ANNOUNCEMENT_PATTERN.replace(cleanedContent) { "**${it.groupValues[2]}**" }

        SCHEDULE_PATTERN.findAll(content).forEach { match ->
            val id = match.groupValues[1].toLongOrNull()
            val title = match.groupValues[2]
            val url = match.groupValues[3]
            if (id != null) {
                references.add(Reference(type = ReferenceType.SCHEDULE, id = id, title = title, url = url))
            }
        }
        cleanedContent = SCHEDULE_PATTERN.replace(cleanedContent) { "**${it.groupValues[2]}**" }

        return ParseResult(
            cleanedContent = cleanedContent,
            references = references.distinctBy { "${it.type}_${it.id}" },
        )
    }
}

data class ParseResult(
    val cleanedContent: String,
    val references: List<Reference>,
)

data class Reference(
    val type: ReferenceType,
    val id: Long,
    val title: String,
    val url: String?,
)

enum class ReferenceType {
    BMC,
    ANALYSIS,
    ANNOUNCEMENT,
    SCHEDULE,
}
