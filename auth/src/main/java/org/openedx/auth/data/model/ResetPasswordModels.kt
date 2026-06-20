package org.openedx.auth.data.model

import com.google.gson.annotations.SerializedName

data class ValidateTokenRequest(
    @SerializedName("token")
    val token: String
)

data class ValidateTokenResponse(
    @SerializedName("is_valid")
    val isValid: Boolean
)

data class PasswordValidationRequest(
    @SerializedName("reset_password_page")
    val resetPasswordPage: Boolean = true,
    @SerializedName("password")
    val password: String
)

data class ResetPasswordConfirmRequest(
    @SerializedName("new_password1")
    val newPassword1: String,
    @SerializedName("new_password2")
    val newPassword2: String
)
