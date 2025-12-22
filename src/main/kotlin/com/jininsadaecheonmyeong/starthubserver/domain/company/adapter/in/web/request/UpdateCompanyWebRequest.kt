package com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.`in`.web.request

import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.UpdateCompanyCommand
import com.jininsadaecheonmyeong.starthubserver.global.support.Phone
import jakarta.validation.constraints.Email

/**
 * 회사 정보 수정 Web Request
 * - Web 계층의 요청을 Command로 변환
 */
data class UpdateCompanyWebRequest(
    val companyDescription: String? = null,
    val businessDescription: String? = null,
    val pageUrl: String? = null,
    @field:Email
    val email: String? = null,
    @field:Phone
    val tel: String? = null,
    val address: String? = null,
    val employeeCount: Int? = null,
    val logoImage: String? = null
) {
    /**
     * Web Request -> Command 변환
     */
    fun toCommand(companyId: Long) = UpdateCompanyCommand(
        companyId = companyId,
        companyDescription = companyDescription,
        businessDescription = businessDescription,
        pageUrl = pageUrl,
        email = email,
        tel = tel,
        address = address,
        employeeCount = employeeCount,
        logoImage = logoImage
    )
}
