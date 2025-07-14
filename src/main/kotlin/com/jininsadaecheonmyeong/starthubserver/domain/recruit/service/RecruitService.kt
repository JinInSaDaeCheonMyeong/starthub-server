
package com.jininsadaecheonmyeong.starthubserver.domain.recruit.service

import com.jininsadaecheonmyeong.starthubserver.domain.company.exception.CompanyNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.company.repository.CompanyRepository
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request.CreateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request.UpdateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response.RecruitResponse
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response.RecruitSummaryResponse
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity.Recruit
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity.RecruitTechStack
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity.TechStack
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.exception.NotRecruitWriterException
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.exception.RecruitNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.repository.RecruitRepository
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.repository.RecruitTechStackRepository
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.repository.TechStackRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecruitService(
    private val recruitRepository: RecruitRepository,
    private val companyRepository: CompanyRepository,
    private val techStackRepository: TechStackRepository,
    private val recruitTechStackRepository: RecruitTechStackRepository,
) {
    @Transactional
    fun createRecruit(request: CreateRecruitRequest): RecruitResponse {
        val user = UserAuthenticationHolder.current()
        val company =
            companyRepository.findByIdAndDeletedFalse(request.companyId)
                .orElseThrow { CompanyNotFoundException("회사를 찾을 수 없습니다.") }

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

    @Transactional(readOnly = true)
    fun getRecruit(recruitId: Long): RecruitResponse {
        val recruit =
            recruitRepository.findByIdAndDeletedFalse(recruitId)
                .orElseThrow { RecruitNotFoundException("채용 공고를 찾을 수 없습니다.") }
        val techStacks = getTechStacksForRecruit(recruit)
        return recruit.toResponse(techStacks)
    }

    @Transactional(readOnly = true)
    fun getAllRecruits(): List<RecruitSummaryResponse> {
        return recruitRepository.findAllByDeletedFalse().map { it.toSummaryResponse() }
    }

    @Transactional
    fun updateRecruit(
        recruitId: Long,
        request: UpdateRecruitRequest,
    ): RecruitResponse {
        val recruit =
            recruitRepository.findByIdAndDeletedFalse(recruitId)
                .orElseThrow { RecruitNotFoundException("채용 공고를 찾을 수 없습니다.") }

        val user = UserAuthenticationHolder.current()
        if (recruit.writer.id != user.id) {
            throw NotRecruitWriterException("채용 공고 작성자만 수정할 수 있습니다.")
        }

        request.title?.let { recruit.title = it }
        request.content?.let { recruit.content = it }
        request.startDate?.let { recruit.startDate = it }
        request.endDate?.let { recruit.endDate = it }
        request.desiredCareer?.let { recruit.desiredCareer = it }
        request.workType?.let { recruit.workType = it }
        request.jobType?.let { recruit.jobType = it }
        request.requiredPeople?.let { recruit.requiredPeople = it }

        val techStacks =
            request.techStack?.let {
                updateRecruitTechStacks(recruit, it)
                it
            } ?: getTechStacksForRecruit(recruit)

        val updatedRecruit = recruitRepository.save(recruit)
        return updatedRecruit.toResponse(techStacks)
    }

    @Transactional
    fun deleteRecruit(recruitId: Long) {
        val recruit =
            recruitRepository.findByIdAndDeletedFalse(recruitId)
                .orElseThrow { RecruitNotFoundException("채용 공고를 찾을 수 없습니다.") }

        val user = UserAuthenticationHolder.current()
        if (recruit.writer.id != user.id) {
            throw NotRecruitWriterException("채용 공고 작성자만 삭제할 수 있습니다.")
        }

        recruit.delete()
        recruitRepository.save(recruit)
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
    ) {
        val existingRecruitTechStacks = recruitTechStackRepository.findByRecruit(recruit)
        recruitTechStackRepository.deleteAll(existingRecruitTechStacks)
        val techStacks = saveAndGetTechStacks(techStackNames)
        saveRecruitTechStacks(recruit, techStacks)
    }
}
