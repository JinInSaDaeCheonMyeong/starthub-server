package com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "bmc_questions")
class BmcQuestion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    val user: User,
    @Column(nullable = false)
    var title: String,
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
    @OneToMany(mappedBy = "bmcQuestion", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var businessModelCanvases: MutableList<BusinessModelCanvas> = mutableListOf(),
) : BaseEntity() {
    fun updateAnswer(
        questionNumber: Int,
        answer: String,
    ) {
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
            question10Answer,
        )
    }

    fun markAsCompleted() {
        this.isCompleted = true
    }

    fun isOwner(user: User): Boolean = this.user.id == user.id
}
