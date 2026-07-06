package org.openedx.core.data.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.openedx.core.domain.model.EnrollmentForm
import org.openedx.core.domain.model.EnrollmentRegistrationField as DomainEnrollmentRegistrationField
import org.openedx.core.domain.model.EnrollmentRegistrationOption as DomainEnrollmentRegistrationOption
import org.openedx.core.domain.model.RegistrationCategory as DomainRegistrationCategory

data class EnrollmentFormResponse(
    @SerializedName("slug")
    val slug: String?,
    @SerializedName("courseid")
    val courseId: String?,
    @SerializedName("pagetitle")
    val pageTitle: String?,
    @SerializedName("templateid")
    val templateId: Int?,
    @SerializedName("infosections")
    val infoSections: String?,
    @SerializedName("eligibility_note")
    val eligibilityNote: String?,
    @SerializedName("result")
    val result: List<RegistrationCategory>?
) {
    fun mapToDomain(): EnrollmentForm {
        return EnrollmentForm(
            slug = slug ?: "",
            courseId = courseId ?: "",
            pageTitle = pageTitle ?: "",
            infoSections = infoSections ?: "",
            eligibilityNote = eligibilityNote ?: "",
            categories = result?.map { it.mapToDomain() } ?: emptyList()
        )
    }
}

data class RegistrationCategory(
    @SerializedName("id")
    val id: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("categoryorder")
    val categoryOrder: Int?,
    @SerializedName("fields")
    val fields: List<EnrollmentRegistrationField>?,
    @SerializedName("description")
    val description: String?
) {
    fun mapToDomain(): DomainRegistrationCategory {
        return DomainRegistrationCategory(
            id = id ?: "",
            title = title ?: "",
            categoryOrder = categoryOrder ?: 0,
            fields = fields?.map { it.mapToDomain() } ?: emptyList(),
            description = description ?: ""
        )
    }
}

data class EnrollmentRegistrationField(
    @SerializedName("name")
    val name: String?,
    @SerializedName("label")
    val label: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("required")
    val required: Boolean?,
    @SerializedName("placeholder")
    val placeholder: String?,
    @SerializedName("iseligibilityfield")
    val isEligibilityField: Boolean?,
    @SerializedName("visible")
    val visible: Boolean?,
    @SerializedName("builtin")
    val builtin: Boolean?,
    @SerializedName("options")
    val options: JsonElement?, // Can be List<RegistrationOption> or Map<String, List<RegistrationOption>>
    @SerializedName("isformaccesssource")
    val isFormAccessSource: Boolean?,
    @SerializedName("allowother")
    val allowOther: Boolean?,
    @SerializedName("helper")
    val helper: String?,
    @SerializedName("dependsOn")
    val dependsOn: String?,
    @SerializedName("maxselections")
    val maxSelections: Int?
) {
    fun mapToDomain(): DomainEnrollmentRegistrationField {
        return DomainEnrollmentRegistrationField(
            name = name ?: "",
            label = label ?: "",
            type = type ?: "",
            required = required ?: false,
            placeholder = placeholder ?: "",
            isEligibilityField = isEligibilityField ?: false,
            visible = visible ?: true,
            builtin = builtin ?: false,
            options = options,
            isFormAccessSource = isFormAccessSource ?: false,
            allowOther = allowOther ?: false,
            helper = helper ?: "",
            dependsOn = dependsOn ?: "",
            maxSelections = maxSelections ?: 1
        )
    }
}

data class EnrollmentRegistrationOption(
    @SerializedName("id")
    val id: String?,
    @SerializedName("value")
    val value: String?,
    @SerializedName("label")
    val label: String?
) {
    fun mapToDomain(): DomainEnrollmentRegistrationOption {
        return DomainEnrollmentRegistrationOption(
            id = id ?: "",
            value = value ?: "",
            label = label ?: ""
        )
    }
}
