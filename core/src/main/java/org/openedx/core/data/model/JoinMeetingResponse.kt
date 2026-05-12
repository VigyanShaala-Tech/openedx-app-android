package org.openedx.core.data.model

import com.google.gson.annotations.SerializedName

data class JoinMeetingResponse(
    @SerializedName("meeting_url")
    val meetingUrl: String
)
