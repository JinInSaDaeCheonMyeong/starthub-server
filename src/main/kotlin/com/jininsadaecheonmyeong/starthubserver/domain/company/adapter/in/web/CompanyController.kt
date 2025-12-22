package com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.`in`.web

import com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.`in`.web.request.CreateCompanyWebRequest
import com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.`in`.web.request.UpdateCompanyWebRequest
import com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.`in`.web.response.CompanyListWebResponse
import com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.`in`.web.response.CompanyWebResponse
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.CreateCompanyUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.DeleteCompanyUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.GetCompanyUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.GetMyCompaniesUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.UpdateCompanyUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.company.docs.CompanyDocs
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Company Web Adapter (Controller)
 * - UseCase 인터페이스를 통한 의존성 주입
 * - Web Request -> Command 변환
 * - Domain Model -> Web Response 변환
 */
@RestController
@RequestMapping("/company")
class CompanyController(
    private val createCompanyUseCase: CreateCompanyUseCase,
    private val updateCompanyUseCase: UpdateCompanyUseCase,
    private val deleteCompanyUseCase: DeleteCompanyUseCase,
    private val getCompanyUseCase: GetCompanyUseCase,
    private val getMyCompaniesUseCase: GetMyCompaniesUseCase
) : CompanyDocs {

    @PostMapping
    override fun save(
        @RequestBody @Valid req: CreateCompanyWebRequest
    ): ResponseEntity<BaseResponse<Unit>> {
        createCompanyUseCase.createCompany(req.toCommand())
        return BaseResponse.of(Unit, "기업 등록 성공")
    }

    @DeleteMapping("/{id}")
    override fun delete(
        @PathVariable id: Long
    ): ResponseEntity<BaseResponse<Unit>> {
        deleteCompanyUseCase.deleteCompany(id)
        return BaseResponse.of(Unit, "기업 삭제 성공")
    }

    @PatchMapping("/{id}")
    override fun update(
        @PathVariable id: Long,
        @RequestBody @Valid req: UpdateCompanyWebRequest
    ): ResponseEntity<BaseResponse<Unit>> {
        updateCompanyUseCase.updateCompany(req.toCommand(id))
        return BaseResponse.of(Unit, "기업 정보 수정 성공")
    }

    @GetMapping("/all")
    override fun getAll(): ResponseEntity<BaseResponse<List<CompanyListWebResponse>>> {
        val companies = getCompanyUseCase.getAll()
        return BaseResponse.of(
            companies.map { CompanyListWebResponse.from(it) },
            "모든 기업 조회 성공"
        )
    }

    @GetMapping("/{id}")
    override fun getById(
        @PathVariable id: Long
    ): ResponseEntity<BaseResponse<CompanyWebResponse?>> {
        val company = getCompanyUseCase.getById(id)
        return BaseResponse.of(
            company?.let { CompanyWebResponse.from(it) },
            "기업 조회 성공"
        )
    }

    @GetMapping(params = ["name"])
    override fun getByName(
        @RequestParam name: String
    ): ResponseEntity<BaseResponse<CompanyWebResponse?>> {
        val company = getCompanyUseCase.getByName(name)
        return BaseResponse.of(
            company?.let { CompanyWebResponse.from(it) },
            "기업 검색 성공"
        )
    }

    @GetMapping(params = ["category"])
    override fun getByCategory(
        @RequestParam category: BusinessType
    ): ResponseEntity<BaseResponse<List<CompanyListWebResponse>>> {
        val companies = getCompanyUseCase.getByCategory(category)
        return BaseResponse.of(
            companies.map { CompanyListWebResponse.from(it) },
            "기업 검색 성공"
        )
    }

    @GetMapping("/my")
    override fun getMy(): ResponseEntity<BaseResponse<List<CompanyListWebResponse>>> {
        val companies = getMyCompaniesUseCase.getMyCompanies()
        return BaseResponse.of(
            companies.map { CompanyListWebResponse.from(it) },
            "등록한 기업 검색 성공"
        )
    }
}
