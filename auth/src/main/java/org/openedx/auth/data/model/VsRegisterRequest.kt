package org.openedx.auth.data.model

import com.google.gson.annotations.SerializedName
data class VsRegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("password") val password: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("terms_of_service") val termsOfService: Boolean = true,
    @SerializedName("honor_code") val honorCode: Boolean = true,
    @SerializedName("user_role") val userRole: String?,
    @SerializedName("username") val username: String,
    @SerializedName("verification_key") val verificationKey: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("social_auth_provider") val socialAuthProvider: String? = null,
    @SerializedName("total_registration_time") val totalRegistrationTime: String? = null,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("provider") val provider: String? = null,
    @SerializedName("client_id") val clientId: String? = null
)
