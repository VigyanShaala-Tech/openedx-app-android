package org.openedx.auth.data.model

import com.google.gson.annotations.SerializedName

data class AccountActivationResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("is_authenticated")
    val isAuthenticated: Boolean?,
    @SerializedName("support_url")
    val supportUrl: String?
)
