package org.openedx.learn.presentation

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openedx.DashboardNavigator
import org.openedx.core.config.Config
import org.openedx.dashboard.presentation.DashboardAnalytics
import org.openedx.dashboard.presentation.DashboardAnalyticsEvent
import org.openedx.dashboard.presentation.DashboardAnalyticsKey
import org.openedx.dashboard.presentation.DashboardRouter
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.model.NotificationModel
import org.openedx.dashboard.data.model.NotificationDto
import org.openedx.dashboard.domain.interactor.DashboardInteractor
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.learn.LearnType

class LearnViewModel(
    openTab: String,
    private val config: Config,
    private val dashboardRouter: DashboardRouter,
    private val analytics: DashboardAnalytics,
    private val corePreferences: CorePreferences,
    private val dashboardInteractor: DashboardInteractor
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(
        LearnUIState(
            if (openTab == LearnTab.PROGRAMS.name) {
                LearnType.PROGRAMS
            } else {
                LearnType.COURSES
            }
        )
    )

    val uiState: StateFlow<LearnUIState>
        get() = _uiState.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _haveNewNotification = MutableStateFlow(false)
    val haveNewNotification = _haveNewNotification.asStateFlow()

    private val dashboardType get() = config.getDashboardConfig().getType()
    val isProgramTypeWebView get() = config.getProgramConfig().isViewTypeWebView()

    fun onSettingsClick(fragmentManager: FragmentManager) {
        dashboardRouter.navigateToSettings(fragmentManager)
    }

    val getDashboardFragment get() = DashboardNavigator(dashboardType).getDashboardFragment()

    val getProgramFragment get() = dashboardRouter.getProgramFragment()

    val userName: String
        get() = corePreferences.user?.name ?: ""

    init {
        fetchNotifications()
        viewModelScope.launch {
            _uiState.collect { uiState ->
                if (uiState.learnType == LearnType.COURSES) {
                    logMyCoursesTabClickedEvent()
                } else {
                    logMyProgramsTabClickedEvent()
                }
            }
        }
    }

    fun updateLearnType(learnType: LearnType) {
        viewModelScope.launch {
            _uiState.update { it.copy(learnType = learnType) }
        }
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            try {
                val response = dashboardInteractor.getNotifications(false)
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
                _haveNewNotification.value = response.haveNewNotification
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            try {
                dashboardInteractor.getNotifications(true)
                _haveNewNotification.value = false
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun logMyCoursesTabClickedEvent() {
        logScreenEvent(DashboardAnalyticsEvent.MY_COURSES)
    }

    fun logMyProgramsTabClickedEvent() {
        logScreenEvent(DashboardAnalyticsEvent.MY_PROGRAMS)
    }

    private fun logScreenEvent(event: DashboardAnalyticsEvent) {
        analytics.logScreenEvent(
            screenName = event.eventName,
            params = buildMap {
                put(DashboardAnalyticsKey.NAME.key, event.biValue)
            }
        )
    }
}
