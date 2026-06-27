package org.openedx.course.data.model

import com.google.gson.annotations.SerializedName

data class CourseNotificationsResponse(
    @SerializedName("haveNewNotification")
    val haveNewNotification: Boolean,
    @SerializedName("notifications")
    val notifications: List<CourseNotificationItem>
)

data class CourseNotificationItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("is_read")
    val isRead: Boolean,
    @SerializedName("created_at")
    val createdAt: String
)
