package com.jininsadaecheonmyeong.starthubserver.domain.recruit.service

import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.exception.CompanyNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.out.persistence.repository.CompanyJpaRepository
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request.CreateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request.UpdateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response.RecruitPreviewResponse
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response.RecruitResponse
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity.Recruit
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity.RecruitTechStack
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity.TechStack
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.exception.NotRecruitWriterException
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.exception.RecruitNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.repository.RecruitRepository
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.repository.RecruitTechStackRepository
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.repository.TechStackRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class RecruitService(
    private val recruitRepository: RecruitRepository,
    private val companyRepository: CompanyJpaRepository,
    private val techStackRepository: TechStackRepository,
    private val recruitTechStackRepository: RecruitTechStackRepository,
    private val userAuthenticationHolder: UserAuthenticationHolder,
) {
    @Transactional
    fun createRecruit(request: CreateRecruitRequest): RecruitResponse {
        val user = userAuthenticationHolder.current()
        val company =
            companyRepository.findByIdAndDeletedFalse(request.companyId)
                ?: throw CompanyNotFoundException("회사를 찾을 수 없습니다.")

        val recruit =
            Recruit(
                title = request.title,
                content = request.content,
                writer = user,
                company = company,
                startDate = request.startDate,
                endDate = request.endDate,
                desiredCareer = request.desiredCareer,
                workType = request.workType,
                jobType = request.jobType,
                requiredPeople = request.requiredPeople,
            )

        val savedRecruit = recruitRepository.save(recruit)
        val techStacks = saveAndGetTechStacks(request.techStack)
        saveRecruitTechStacks(savedRecruit, techStacks)

        return savedRecruit.toResponse(techStacks.map { it.name })
    }

    @Transactional
    fun updateRecruit(
        recruitId: Long,
        request: UpdateRecruitRequest,
    ): RecruitResponse {
        val recruit =
            recruitRepository.findByIdAndDeletedFalse(recruitId)
                ?: throw RecruitNotFoundException("채용 공고를 찾을 수 없습니다.")

        val user = userAuthenticationHolder.current()
        if (recruit.writer.id != user.id) {
            throw NotRecruitWriterException("채용 공고 작성자만 수정할 수 있습니다.")
        }

        recruit.title = request.title
        recruit.content = request.content
        recruit.startDate = request.startDate
        recruit.endDate = request.endDate
        recruit.desiredCareer = request.desiredCareer
        recruit.workType = request.workType
        recruit.jobType = request.jobType
        recruit.requiredPeople = request.requiredPeople

        val techStacks = updateRecruitTechStacks(recruit, request.techStack)

        val updatedRecruit = recruitRepository.save(recruit)
        return updatedRecruit.toResponse(techStacks.map { it.name })
    }

    @Transactional
    fun deleteRecruit(recruitId: Long) {
        val recruit =
            recruitRepository.findByIdAndDeletedFalse(recruitId)
                ?: throw RecruitNotFoundException("채용 공고를 찾을 수 없습니다.")

        val user = userAuthenticationHolder.current()
        if (recruit.writer.id != user.id) {
            throw NotRecruitWriterException("채용 공고 작성자만 삭제할 수 있습니다.")
        }

        recruit.delete()
        recruitRepository.save(recruit)
    }

    @Transactional
    fun closeRecruit(recruitId: Long) {
        val recruit =
            recruitRepository.findByIdAndDeletedFalse(recruitId)
                ?: throw RecruitNotFoundException("채용 공고를 찾을 수 없습니다.")

        val user = userAuthenticationHolder.current()
        if (recruit.writer.id != user.id) {
            throw NotRecruitWriterException("채용 공고 작성자만 마감할 수 있습니다.")
        }

        recruit.close()
        recruitRepository.save(recruit)
    }

    fun getAllRecruits(
        page: Int,
        size: Int,
    ): Page<RecruitPreviewResponse> {
        val pageable = PageRequest.of(page, size)
        return recruitRepository.findAllByDeletedFalse(pageable).map { it.toSummaryResponse() }
    }

    @Transactional
    fun getRecruit(recruitId: Long): RecruitResponse {
        val recruit =
            recruitRepository.findByIdAndDeletedFalse(recruitId)
                ?: throw RecruitNotFoundException("채용 공고를 찾을 수 없습니다.")
        recruit.viewCount++
        val techStacks = getTechStacksForRecruit(recruit)
        return recruit.toResponse(techStacks)
    }

    private fun saveAndGetTechStacks(techStackNames: List<String>): List<TechStack> {
        return techStackNames.map { techName ->
            techStackRepository.findByName(techName) ?: techStackRepository.save(TechStack(name = techName))
        }
    }

    private fun saveRecruitTechStacks(
        recruit: Recruit,
        techStacks: List<TechStack>,
    ) {
        techStacks.forEach { techStack ->
            recruitTechStackRepository.save(RecruitTechStack(recruit = recruit, techStack = techStack))
        }
    }

    private fun getTechStacksForRecruit(recruit: Recruit): List<String> {
        return recruitTechStackRepository.findByRecruit(recruit).map { it.techStack.name }
    }

    private fun updateRecruitTechStacks(
        recruit: Recruit,
        techStackNames: List<String>,
    ): List<TechStack> {
        recruitTechStackRepository.deleteAll(recruitTechStackRepository.findByRecruit(recruit))
        val techStacks = saveAndGetTechStacks(techStackNames)
        saveRecruitTechStacks(recruit, techStacks)
        return techStacks
    }
}
