package org.openedx.core.data.model

import com.google.gson.annotations.SerializedName

data class LiveClassResponse(
    @SerializedName("results")
    val results: List<LiveClassModel>,
    @SerializedName("pagination")
    val pagination: Pagination
)

data class LiveClassModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("topic")
    val topic: String,
    @SerializedName("is_owner")
    val isOwner: Boolean,
    @SerializedName("is_recurring_meeting")
    val isRecurringMeeting: Boolean,
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("duration_hours")
    val durationHours: Int,
    @SerializedName("duration_minutes")
    val durationMinutes: Int,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("occurrence_internal_id")
    val occurrenceInternalId: String
)
