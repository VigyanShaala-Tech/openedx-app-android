package org.openedx.core.data.model

import com.google.gson.annotations.SerializedName
import org.openedx.core.domain.model.NotificationListResponse
import org.openedx.core.domain.model.NotificationModel as DomainNotificationModel

data class CourseNotificationsResponse(
    @SerializedName("haveNewNotification")
    val haveNewNotification: Boolean,
    @SerializedName("notifications")
    val notifications: List<CourseNotificationItem>
) {
    fun mapToDomain(): NotificationListResponse {
        return NotificationListResponse(
            haveNewNotification = haveNewNotification,
            notifications = notifications.map { it.mapToDomain() }
        )
    }
}

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
) {
    fun mapToDomain(): DomainNotificationModel {
        return DomainNotificationModel(
            id = id,
            title = title,
            description = description,
            type = type,
            isRead = isRead,
            createdAt = createdAt
        )
    }
}
