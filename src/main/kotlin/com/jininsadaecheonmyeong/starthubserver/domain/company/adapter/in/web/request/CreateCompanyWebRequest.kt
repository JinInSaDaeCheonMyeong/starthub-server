package com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.`in`.web.request

import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.CreateCompanyCommand
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.global.support.Phone
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min

/**
 * 회사 생성 Web Request
 * - Web 계층의 요청을 Command로 변환
 */
data class CreateCompanyWebRequest(
    val name: String,
    val companyDescription: String,
    val category: BusinessType,
    val businessDescription: String,
    val pageUrl: String? = null,
    @field:Email
    val email: String,
    @field:Phone
    val tel: String,
    val address: String? = null,
    @field:Min(value = 1, message = "사원 수 누락됨")
    val employeeCount: Int,
    val logoImage: String? = null
) {
    /**
     * Web Request -> Command 변환
     */
    fun toCommand() = CreateCompanyCommand(
        name = name,
        companyDescription = companyDescription,
        category = category,
        businessDescription = businessDescription,
        pageUrl = pageUrl,
        email = email,
        tel = tel,
        address = address,
        employeeCount = employeeCount,
        logoImage = logoImage
    )
}
