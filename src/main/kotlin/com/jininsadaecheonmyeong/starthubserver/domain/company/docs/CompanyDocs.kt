package com.jininsadaecheonmyeong.starthubserver.domain.company.docs

import com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.`in`.web.request.CreateCompanyWebRequest
import com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.`in`.web.request.UpdateCompanyWebRequest
import com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.`in`.web.response.CompanyListWebResponse
import com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.`in`.web.response.CompanyWebResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "기업", description = "기업 관련 API")
interface CompanyDocs {
    @Operation(summary = "기업 등록", description = "기업을 등록합니다.")
    fun save(
        @RequestBody req: CreateCompanyWebRequest,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(summary = "기업 삭제", description = "기업의 UUID로 삭제합니다.")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(summary = "기업 정보 업데이트", description = "기업 프로필을 업데이트합니다.")
    fun update(
        @PathVariable id: Long,
        @RequestBody req: UpdateCompanyWebRequest,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(summary = "모든 기업 조회", description = "모든 기업들을 조회합니다.")
    fun getAll(): ResponseEntity<BaseResponse<List<CompanyListWebResponse>>>

    @Operation(summary = "아이디로 기업 조회", description = "기업의 UUID로 세부 정보를 조회합니다.")
    fun getById(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<CompanyWebResponse?>>

    @Operation(summary = "이름으로 기업 조회", description = "파라미터에 이름을 담아 요청해야 합니다.")
    fun getByName(
        @RequestParam name: String,
    ): ResponseEntity<BaseResponse<CompanyWebResponse?>>

    @Operation(summary = "카테고리로 기업 조회", description = "파라미터에 기업 카테고리를 담아 요청해야 합니다.")
    fun getByCategory(
        @RequestParam category: BusinessType,
    ): ResponseEntity<BaseResponse<List<CompanyListWebResponse>>>

    @Operation(summary = "나의 기업 조회", description = "로그인 된 사용자의 기업을 조회합니다.")
    fun getMy(): ResponseEntity<BaseResponse<List<CompanyListWebResponse>>>
}
