package com.jininsadaecheonmyeong.starthubserver.domain.company.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.company.data.request.CreateCompanyRequest
import com.jininsadaecheonmyeong.starthubserver.domain.company.data.request.UpdateCompanyProfileRequest
import com.jininsadaecheonmyeong.starthubserver.domain.company.data.response.CompanyListResponse
import com.jininsadaecheonmyeong.starthubserver.domain.company.data.response.CompanyResponse
import com.jininsadaecheonmyeong.starthubserver.domain.company.docs.CompanyDocs
import com.jininsadaecheonmyeong.starthubserver.domain.company.service.CompanyService
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/company")
class CompanyController(
    private val service: CompanyService,
) : CompanyDocs {
    @PostMapping
    override fun save(
        @RequestBody @Valid req: CreateCompanyRequest,
    ) = BaseResponse.of(service.save(req), "기업 등록 성공")

    @DeleteMapping("/{id}")
    override fun delete(
        @PathVariable id: Long,
    ) = BaseResponse.of(service.delete(id), "기업 삭제 성공")

    @PatchMapping("/{id}")
    override fun update(
        @PathVariable id: Long,
        @RequestBody @Valid req: UpdateCompanyProfileRequest,
    ) = BaseResponse.of(service.update(id, req), "기업 정보 수정 성공")

    @GetMapping("/all")
    override fun getAll(): ResponseEntity<BaseResponse<List<CompanyListResponse>>> =
        BaseResponse.of(service.findAll().map { it.toCompanyListResponse() }, "모든 기업 조회 성공")

    @GetMapping("/{id}")
    override fun getById(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<CompanyResponse?>> {
        val company = service.findById(id)
        val response = company?.toCompanyResponse()
        return BaseResponse.of(response, "기업 조회 성공")
    }

    @GetMapping(params = ["name"])
    override fun getByName(
        @RequestParam name: String,
    ): ResponseEntity<BaseResponse<CompanyResponse?>> {
        val company = service.findByCompanyName(name)
        val response = company?.toCompanyResponse()
        return BaseResponse.of(response, "기업 검색 성공")
    }

    @GetMapping(params = ["category"])
    override fun getByCategory(
        @RequestParam category: BusinessType,
    ): ResponseEntity<BaseResponse<List<CompanyListResponse>>> =
        BaseResponse.of(service.findByCategory(category).map { it.toCompanyListResponse() }, "기업 검색 성공")

    @GetMapping("/my")
    override fun getMy(): ResponseEntity<BaseResponse<List<CompanyListResponse>>> =
        BaseResponse.of(service.findMy().map { it.toCompanyListResponse() }, "등록한 기업 검색 성공")
}
