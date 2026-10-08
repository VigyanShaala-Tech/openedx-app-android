package org.openedx.dashboard.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.core.system.notifier.CourseDashboardUpdate
import org.openedx.core.system.notifier.DiscoveryNotifier
import org.openedx.core.system.notifier.NavigationToDiscovery
import org.openedx.dashboard.data.model.AchievementDto
import org.openedx.dashboard.data.model.CourseItemDto
import org.openedx.dashboard.data.model.PaginatedDto
import org.openedx.dashboard.data.model.RecommendationDto
import org.openedx.dashboard.data.model.SummaryCardDto
import org.openedx.dashboard.data.model.WishlistItemData
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
    val wishlist: PaginatedDto<WishlistItemData>? = null,
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
    private val discoveryNotifier: DiscoveryNotifier,
) : BaseViewModel() {

    val apiHostUrl get() = config.getApiHostURL()
    val hasInternetConnection: Boolean get() = networkConnection.isOnline()
    val isTablet get() = windowSize.isTablet
    val userName: String
        get() = corePreferences.user?.username ?: "Learner"
    val userFullName: String?
        get() = corePreferences.user?.name

    private val _uiMessage = kotlinx.coroutines.flow.MutableSharedFlow<UIMessage>()
    val uiMessage = _uiMessage

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(NewDashboardState())
    val state = _state

    init {
        loadAll(isRefreshing = false)
        viewModelScope.launch {
            discoveryNotifier.notifier.collect {
                if (it is CourseDashboardUpdate) {
                    refresh()
                }
            }
        }
    }

    fun refresh() {
        loadAll(isRefreshing = true)
    }

    private fun loadAll(isRefreshing: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = !isRefreshing, refreshing = isRefreshing)
            if (!networkConnection.isOnline()) {
                loadFromCache()
                _uiMessage.emit(
                    UIMessage.SnackBarMessage(resourceManager.getString(org.openedx.core.R.string.core_error_no_connection))
                )
                return@launch
            }
            try {
                kotlinx.coroutines.supervisorScope {
                    val summary = async { interactor.getSummaryCards() }
                    val contLearn = async { interactor.getContinueLearning() }
                    val achievements = async { interactor.getAchievements() }
                    val recommended = async { interactor.getRecommended() }
                    val wishlist = async { interactor.getWishlist() }
                    val inProgress = async { interactor.getInProgress() }
                    val completed = async { interactor.getCompleted() }
                    val enrolledCoursesJob = async { runCatching { interactor.getEnrolledCourses(1) }.getOrNull() }

                    val summaryVal = try {
                        summary.await()
                    } catch (e: Exception) {
                        emptyList()
                    }
                    var contLearnVal = try {
                        contLearn.await()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    val enrolledList = enrolledCoursesJob.await()

                    val inProgressVal = try {
                        inProgress.await()
                    } catch (e: Exception) {
                        null
                    }
                    val completedVal = try {
                        completed.await()
                    } catch (e: Exception) {
                        null
                    }

                    if (contLearnVal.isEmpty()) {
                        val enrolledCourses = enrolledList?.courses ?: runCatching { interactor.getEnrolledCoursesFromCache() }.getOrDefault(emptyList())
                        if (enrolledCourses.isNotEmpty()) {
                            contLearnVal = enrolledCourses.map { enrolled ->
                                CourseItemDto(
                                    id = enrolled.course.id,
                                    title = enrolled.course.name,
                                    course_image = enrolled.course.courseImage,
                                    progress = enrolled.progress.value.toInt(),
                                    category = "",
                                    level = ""
                                )
                            }
                        }
                    }

                    if (networkConnection.isOnline() && !isRefreshing &&
                        (inProgressVal?.results?.isEmpty() ?: true) &&
                        (completedVal?.results?.isEmpty() ?: true) &&
                        contLearnVal.isEmpty()
                    ) {
                        discoveryNotifier.send(NavigationToDiscovery())
                    }

                    _state.value = _state.value.copy(
                        loading = false,
                        refreshing = false,
                        summary = summaryVal,
                        continueLearning = contLearnVal,
                        achievements = try {
                            achievements.await()
                        } catch (e: Exception) {
                            emptyList()
                        },
                        recommended = try {
                            recommended.await()
                        } catch (e: Exception) {
                            emptyList()
                        },
                        wishlist = try {
                            wishlist.await()
                        } catch (e: Exception) {
                            null
                        },
                        inProgress = inProgressVal ?: if (contLearnVal.isNotEmpty()) PaginatedDto(results = contLearnVal) else null,
                        completed = completedVal,
                    )
                }
            } catch (e: Exception) {
                loadFromCache()
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

    private suspend fun loadFromCache() {
        try {
            val cachedCourses = interactor.getEnrolledCoursesFromCache()
            if (cachedCourses.isNotEmpty()) {
                val cachedItems = cachedCourses.map { enrolled ->
                    CourseItemDto(
                        id = enrolled.course.id,
                        title = enrolled.course.name,
                        course_image = enrolled.course.courseImage,
                        progress = enrolled.progress.value.toInt(),
                        category = "",
                        level = ""
                    )
                }
                _state.value = _state.value.copy(
                    loading = false,
                    refreshing = false,
                    continueLearning = cachedItems,
                    inProgress = PaginatedDto(
                        results = cachedItems
                    )
                )
            } else {
                _state.value = _state.value.copy(loading = false, refreshing = false)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(loading = false, refreshing = false)
        }
    }

    fun removeFromWishlist(courseId: String) {
        viewModelScope.launch {
            try {
                interactor.removeFromWishlist(courseId)
                val current = _state.value.wishlist
                if (current != null) {
                    val updated = current.copy(
                        results = current.results.filterNot { it.id == courseId }
                    )
                    _state.value = _state.value.copy(wishlist = updated)
                }
            } catch (e: Exception) {
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
