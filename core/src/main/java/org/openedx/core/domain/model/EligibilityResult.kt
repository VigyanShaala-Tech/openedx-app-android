package org.openedx.core.domain.model

data class EligibilityResult(
    val isEligible: Boolean,
    val message: String
)
