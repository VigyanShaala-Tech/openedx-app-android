package org.openedx.core.data.model

import com.google.gson.annotations.SerializedName

data class OngoingSessionResponse(
    @SerializedName("result")
    val result: OngoingSessionModel
)

data class OngoingSessionModel(
    @SerializedName("isSessionOngoing")
    val isSessionOngoing: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("link")
    val link: String?,
    @SerializedName("linkLabel")
    val linkLabel: String?,
    @SerializedName("meetingName")
    val meetingName: String?,
    @SerializedName("Started")
    val started: String?
)
