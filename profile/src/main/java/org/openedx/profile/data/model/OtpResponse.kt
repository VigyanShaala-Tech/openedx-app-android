package org.openedx.profile.data.model

import com.google.gson.annotations.SerializedName

data class OtpSendResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("verification_key")
    val verification_key: String?,
    @SerializedName("resend_after_seconds")
    val resend_after_seconds: Int?,
    @SerializedName("expires_in_seconds")
    val expires_in_seconds: Int?
)

data class OtpVerifyResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("verification_key")
    val verification_key: String?
)
