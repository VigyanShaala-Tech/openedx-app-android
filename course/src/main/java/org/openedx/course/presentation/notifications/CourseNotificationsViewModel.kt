package org.openedx.course.presentation.notifications

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.course.data.model.CourseNotificationItem
import org.openedx.course.domain.interactor.LeaderboardInteractor

class CourseNotificationsViewModel(
    val courseId: String,
    private val leaderboardInteractor: LeaderboardInteractor
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<CourseNotificationsUIState>(CourseNotificationsUIState.Loading)
    val uiState: StateFlow<CourseNotificationsUIState> = _uiState.asStateFlow()

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        _uiState.value = CourseNotificationsUIState.Loading
        viewModelScope.launch {
            try {
                val response = leaderboardInteractor.getCourseNotifications(courseId)
                _uiState.value = CourseNotificationsUIState.Success(response.notifications)
            } catch (e: Exception) {
                _uiState.value = CourseNotificationsUIState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class CourseNotificationsUIState {
    object Loading : CourseNotificationsUIState()
    data class Success(val notifications: List<CourseNotificationItem>) : CourseNotificationsUIState()
    data class Error(val message: String) : CourseNotificationsUIState()
}
