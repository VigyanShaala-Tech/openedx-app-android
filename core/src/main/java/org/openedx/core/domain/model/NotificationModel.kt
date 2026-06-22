package org.openedx.core.domain.model

data class NotificationModel(
    val id: Int,
    val title: String,
    val description: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: String
)

data class NotificationListResponse(
    val haveNewNotification: Boolean,
    val notifications: List<NotificationModel>
)
