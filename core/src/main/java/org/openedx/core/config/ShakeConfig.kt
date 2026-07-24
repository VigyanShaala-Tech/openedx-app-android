package org.openedx.core.config

import com.google.gson.annotations.SerializedName

data class ShakeConfig(
    @SerializedName("ENABLED")
    val enabled: Boolean = false,
    @SerializedName("TOKEN")
    val token: String = ""
)
