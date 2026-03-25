package org.openedx.dashboard.presentation.achievements

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.R
import org.openedx.core.config.Config
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.dashboard.data.model.AchievementStatDto
import org.openedx.dashboard.data.model.BadgeProgressDto
import org.openedx.dashboard.data.model.EarnedBadgeDto
import org.openedx.dashboard.domain.interactor.DashboardInteractor
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager

data class AchievementsUIState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val stats: List<AchievementStatDto> = emptyList(),
    val earnedBadges: List<EarnedBadgeDto> = emptyList(),
    val badgesInProgress: List<BadgeProgressDto> = emptyList(),
)

class AchievementsViewModel(
    private val config: Config,
    private val networkConnection: NetworkConnection,
    private val interactor: DashboardInteractor,
    private val resourceManager: ResourceManager,
) : BaseViewModel() {

    val apiHostUrl get() = config.getApiHostURL()
    val hasInternetConnection: Boolean get() = networkConnection.isOnline()

    private val _uiState = MutableStateFlow(AchievementsUIState())
    val uiState: StateFlow<AchievementsUIState> get() = _uiState.asStateFlow()

    private val _uiMessage = MutableSharedFlow<UIMessage>()
    val uiMessage: SharedFlow<UIMessage> get() = _uiMessage.asSharedFlow()

    init {
        loadAchievements(isRefreshing = false)
    }

    fun refresh() {
        loadAchievements(isRefreshing = true)
    }

    private fun loadAchievements(isRefreshing: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = !isRefreshing, refreshing = isRefreshing)
            try {
                val response = interactor.getAllAchievements()
                val fallbackProgress = listOf(
                    BadgeProgressDto(null, "10 Hours of Learning", "Completed 10 hours of learning content.", 70),
                    BadgeProgressDto(null, "Research Pioneer", "Completed your first research project.", 40),
                    BadgeProgressDto(null, "Community Helper", "Helped 5 peers in discussion forums.", 60),
                    BadgeProgressDto(null, "5 Courses Completed", "Completed 5 courses on the platform.", 60),
                )
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    refreshing = false,
                    stats = response.stats,
                    earnedBadges = response.earned_badges,
                    badgesInProgress = if (response.badges_in_progress.isNotEmpty()) response.badges_in_progress else fallbackProgress
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, refreshing = false)
                if (e.isInternetError()) {
                    _uiMessage.emit(
                        UIMessage.SnackBarMessage(resourceManager.getString(R.string.core_error_no_connection))
                    )
                } else {
                    _uiMessage.emit(
                        UIMessage.SnackBarMessage(resourceManager.getString(R.string.core_error_unknown_error))
                    )
                }
            }
        }
    }
}
