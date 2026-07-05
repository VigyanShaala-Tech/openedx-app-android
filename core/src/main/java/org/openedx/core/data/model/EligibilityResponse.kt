package org.openedx.core.data.model

import com.google.gson.annotations.SerializedName

data class EligibilityResponse(
    @SerializedName("is_eligible")
    val isEligible: Boolean?,
    @SerializedName("message")
    val message: String?
)
