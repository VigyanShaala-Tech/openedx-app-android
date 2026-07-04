package org.openedx.course.data.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

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
)

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
)

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
)

data class EnrollmentRegistrationOption(
    @SerializedName("id")
    val id: String?,
    @SerializedName("value")
    val value: String?,
    @SerializedName("label")
    val label: String?
)
