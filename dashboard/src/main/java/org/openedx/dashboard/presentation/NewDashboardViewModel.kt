package org.openedx.dashboard.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.openedx.core.config.Config
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.core.data.storage.CorePreferences
import org.openedx.dashboard.data.model.AchievementDto
import org.openedx.dashboard.data.model.CourseItemDto
import org.openedx.dashboard.data.model.PaginatedDto
import org.openedx.dashboard.data.model.RecommendationDto
import org.openedx.dashboard.data.model.SummaryCardDto
import org.openedx.dashboard.domain.interactor.DashboardInteractor
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.system.ResourceManager

data class NewDashboardState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val summary: List<SummaryCardDto> = emptyList(),
    val continueLearning: List<CourseItemDto> = emptyList(),
    val achievements: List<AchievementDto> = emptyList(),
    val recommended: List<RecommendationDto> = emptyList(),
    val wishlist: PaginatedDto<CourseItemDto>? = null,
    val inProgress: PaginatedDto<CourseItemDto>? = null,
    val completed: PaginatedDto<CourseItemDto>? = null,
)

class NewDashboardViewModel(
    private val config: Config,
    private val interactor: DashboardInteractor,
    private val resourceManager: ResourceManager,
    private val networkConnection: NetworkConnection,
    private val windowSize: WindowSize,
    private val corePreferences: CorePreferences,
) : BaseViewModel() {

    val apiHostUrl get() = config.getApiHostURL()
    val hasInternetConnection: Boolean get() = networkConnection.isOnline()
    val isTablet get() = windowSize.isTablet
    val userName: String
        get() = corePreferences.user?.username?.takeIf { it.isNotBlank() } ?: "Learner"

    private val _uiMessage = kotlinx.coroutines.flow.MutableSharedFlow<UIMessage>()
    val uiMessage = _uiMessage

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(NewDashboardState())
    val state = _state

    init {
        loadAll(isRefreshing = false)
    }

    fun refresh() {
        loadAll(isRefreshing = true)
    }

    private fun loadAll(isRefreshing: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = !isRefreshing, refreshing = isRefreshing)
            try {
                val summary = async { interactor.getSummaryCards() }
                val contLearn = async { interactor.getContinueLearning() }
                val achievements = async { interactor.getAchievements() }
                val recommended = async { interactor.getRecommended() }
                val wishlist = async { interactor.getWishlist() }
                val inProgress = async { interactor.getInProgress() }
                val completed = async { interactor.getCompleted() }

                _state.value = _state.value.copy(
                    loading = false,
                    refreshing = false,
                    summary = summary.await(),
                    continueLearning = contLearn.await(),
                    achievements = achievements.await(),
                    recommended = recommended.await(),
                    wishlist = wishlist.await(),
                    inProgress = inProgress.await(),
                    completed = completed.await(),
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, refreshing = false)
                if (e.isInternetError()) {
                    _uiMessage.emit(
                        UIMessage.SnackBarMessage(resourceManager.getString(org.openedx.core.R.string.core_error_no_connection))
                    )
                } else {
                    _uiMessage.emit(
                        UIMessage.SnackBarMessage(resourceManager.getString(org.openedx.core.R.string.core_error_unknown_error))
                    )
                }
            }
        }
    }
}
