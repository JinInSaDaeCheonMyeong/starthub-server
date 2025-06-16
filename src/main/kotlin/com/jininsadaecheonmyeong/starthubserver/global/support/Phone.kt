package com.jininsadaecheonmyeong.starthubserver.global.support

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PhoneValidator::class])
annotation class Phone(
    val message: String = "잘못된 전화번호 형식입니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class PhoneValidator : ConstraintValidator<Phone, String?> {

    companion object {
        private const val PHONE_PATTERN = "^(01[0-9]-\\d{3,4}-\\d{4}|0[2-9][0-9]{0,1}-\\d{3,4}-\\d{4})$"
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return true
        }

        if (value.isBlank()) {
            return false
        }

        return value.matches(Regex(PHONE_PATTERN))
    }
}
