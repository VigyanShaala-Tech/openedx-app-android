package org.openedx.dashboard.presentation.notifications

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.domain.model.NotificationModel
import org.openedx.dashboard.domain.interactor.DashboardInteractor
import org.openedx.foundation.presentation.BaseViewModel

class NotificationsViewModel(
    private val dashboardInteractor: DashboardInteractor
) : BaseViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = dashboardInteractor.getNotifications(true)
                _notifications.value = response.notifications.map {
                    NotificationModel(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        type = it.type,
                        isRead = it.is_read,
                        createdAt = it.created_at
                    )
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
