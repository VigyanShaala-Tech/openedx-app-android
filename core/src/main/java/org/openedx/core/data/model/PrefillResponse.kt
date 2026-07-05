package org.openedx.core.data.model

import com.google.gson.annotations.SerializedName

data class PrefillResponse(
    @SerializedName("prefill")
    val prefill: Boolean,
    @SerializedName("answers")
    val answers: Map<String, Any>
)
