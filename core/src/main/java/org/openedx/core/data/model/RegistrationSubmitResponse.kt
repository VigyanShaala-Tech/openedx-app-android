package org.openedx.core.data.model

import com.google.gson.annotations.SerializedName

data class RegistrationSubmitResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("useralreadyexists")
    val userAlreadyExists: Boolean,
    @SerializedName("isloggedin")
    val isLoggedIn: Boolean,
    @SerializedName("thanksmessage")
    val thanksMessage: String,
    @SerializedName("loginoptions")
    val loginOptions: Map<String, String>?
)
