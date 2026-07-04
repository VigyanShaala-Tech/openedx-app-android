package org.openedx.core.domain.model

data class EnrollmentForm(
    val slug: String,
    val courseId: String,
    val pageTitle: String,
    val infoSections: String,
    val categories: List<RegistrationCategory>
)

data class RegistrationCategory(
    val id: String,
    val title: String,
    val categoryOrder: Int,
    val fields: List<EnrollmentRegistrationField>,
    val description: String
)

data class EnrollmentRegistrationField(
    val name: String,
    val label: String,
    val type: String,
    val required: Boolean,
    val placeholder: String,
    val isEligibilityField: Boolean,
    val visible: Boolean,
    val builtin: Boolean,
    val options: Any?, // Can be List<EnrollmentRegistrationOption> or Map<String, List<EnrollmentRegistrationOption>>
    val isFormAccessSource: Boolean,
    val allowOther: Boolean,
    val helper: String,
    val dependsOn: String,
    val maxSelections: Int
)

data class EnrollmentRegistrationOption(
    val id: String,
    val value: String,
    val label: String
)
