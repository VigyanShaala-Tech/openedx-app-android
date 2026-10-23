package org.openedx.core.data.model

import com.google.gson.annotations.SerializedName

data class CsrfTokenResponse(
    @SerializedName("csrfToken")
    val csrfToken: String
)
