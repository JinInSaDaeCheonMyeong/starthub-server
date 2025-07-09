package com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "bmc_questions")
class BmcQuestion(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    var sessionId: String,

    @Column(nullable = false)
    var businessIdea: String,

    @Column(columnDefinition = "TEXT")
    var question1Answer: String? = null,

    @Column(columnDefinition = "TEXT")
    var question2Answer: String? = null,

    @Column(columnDefinition = "TEXT")
    var question3Answer: String? = null,

    @Column(columnDefinition = "TEXT")
    var question4Answer: String? = null,

    @Column(columnDefinition = "TEXT")
    var question5Answer: String? = null,

    @Column(columnDefinition = "TEXT")
    var question6Answer: String? = null,

    @Column(columnDefinition = "TEXT")
    var question7Answer: String? = null,

    @Column(columnDefinition = "TEXT")
    var question8Answer: String? = null,

    @Column(columnDefinition = "TEXT")
    var question9Answer: String? = null,

    @Column(columnDefinition = "TEXT")
    var question10Answer: String? = null,

    @Column(nullable = false)
    var isCompleted: Boolean = false,

    @OneToOne(mappedBy = "bmcQuestion", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var businessModelCanvas: BusinessModelCanvas? = null
) : BaseEntity() {

    fun updateAnswer(questionNumber: Int, answer: String) {
        when (questionNumber) {
            1 -> question1Answer = answer
            2 -> question2Answer = answer
            3 -> question3Answer = answer
            4 -> question4Answer = answer
            5 -> question5Answer = answer
            6 -> question6Answer = answer
            7 -> question7Answer = answer
            8 -> question8Answer = answer
            9 -> question9Answer = answer
            10 -> question10Answer = answer
        }
    }

    fun getAnswer(questionNumber: Int): String? {
        return when (questionNumber) {
            1 -> question1Answer
            2 -> question2Answer
            3 -> question3Answer
            4 -> question4Answer
            5 -> question5Answer
            6 -> question6Answer
            7 -> question7Answer
            8 -> question8Answer
            9 -> question9Answer
            10 -> question10Answer
            else -> null
        }
    }

    fun getAllAnswers(): List<String?> {
        return listOf(
            question1Answer,
            question2Answer,
            question3Answer,
            question4Answer,
            question5Answer,
            question6Answer,
            question7Answer,
            question8Answer,
            question9Answer,
            question10Answer
        )
    }

    fun markAsCompleted() {
        this.isCompleted = true
    }

    fun isOwner(user: User): Boolean = this.user.id == user.id
}