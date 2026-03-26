package org.openedx.auth.data.model

import com.google.gson.annotations.SerializedName
data class VsRegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("password") val password: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("terms_of_service") val termsOfService: Boolean = true,
    @SerializedName("user_role") val userRole: String?,
    @SerializedName("username") val username: String,
    @SerializedName("verification_key") val verificationKey: String?
)
