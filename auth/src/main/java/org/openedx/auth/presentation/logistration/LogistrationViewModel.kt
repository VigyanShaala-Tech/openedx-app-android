package org.openedx.auth.presentation.logistration

import android.app.Activity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.openedx.auth.presentation.AuthAnalytics
import org.openedx.auth.presentation.AuthAnalyticsEvent
import org.openedx.auth.presentation.AuthAnalyticsKey
import org.openedx.auth.presentation.AuthRouter
import org.openedx.auth.presentation.sso.BrowserAuthHelper
import org.openedx.core.R
import org.openedx.core.config.Config
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.core.utils.Logger
import org.openedx.discovery.domain.interactor.DiscoveryInteractor
import org.openedx.discovery.domain.model.Course
import org.openedx.discovery.presentation.DiscoveryAnalyticsEvent
import org.openedx.discovery.presentation.DiscoveryAnalyticsKey
import org.openedx.discovery.presentation.DiscoveryUIState
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.extension.takeIfNotEmpty
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.SingleEventLiveData
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager

class LogistrationViewModel(
    private val courseId: String,
    private val router: AuthRouter,
    private val config: Config,
    private val analytics: AuthAnalytics,
    private val browserAuthHelper: BrowserAuthHelper,
    private val discoveryInteractor: DiscoveryInteractor,
    private val resourceManager: ResourceManager,
    private val networkConnection: NetworkConnection,
    private val catalogApi: org.openedx.auth.data.api.CatalogApi,
) : BaseViewModel() {

    private val logger = Logger("LogistrationViewModel")

    private val discoveryTypeWebView get() = config.getDiscoveryConfig().isViewTypeWebView()
    val isRegistrationEnabled get() = config.isRegistrationEnabled()
    val isBrowserRegistrationEnabled get() = config.isBrowserRegistrationEnabled()
    val isBrowserLoginEnabled get() = config.isBrowserLoginEnabled()
    val apiHostUrl get() = config.getApiHostURL()

    private val _uiState = MutableLiveData<DiscoveryUIState>(DiscoveryUIState.Loading)
    val uiState: LiveData<DiscoveryUIState>
        get() = _uiState

    private val _uiMessage = SingleEventLiveData<UIMessage>()
    val uiMessage: LiveData<UIMessage>
        get() = _uiMessage

    private val _canLoadMore = MutableLiveData<Boolean>()
    val canLoadMore: LiveData<Boolean>
        get() = _canLoadMore

    private val _isUpdating = MutableLiveData<Boolean>()
    val isUpdating: LiveData<Boolean>
        get() = _isUpdating

    val hasInternetConnection: Boolean
        get() = networkConnection.isOnline()

    private var page = 1
    private val coursesList = mutableListOf<Course>()
    private var isLoading = false

    init {
        logLogistrationScreenEvent()
        getCoursesList()
    }

    fun getCoursesList(
        username: String? = null,
        organization: String? = null
    ) {
        _uiState.value = DiscoveryUIState.Loading
        coursesList.clear()
        loadCoursesInternal(username, organization)
    }

    private fun loadCoursesInternal(
        username: String? = null,
        organization: String? = null
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                val response = discoveryInteractor.getCoursesList(username, organization, page)
                if (response.pagination.next.isNotEmpty() && page != response.pagination.numPages) {
                    _canLoadMore.value = true
                    page++
                } else {
                    _canLoadMore.value = false
                    page = -1
                }
                coursesList.addAll(response.results)
                _uiState.value = DiscoveryUIState.Courses(ArrayList(coursesList))
            } catch (e: Exception) {
                if (e.isInternetError()) {
                    _uiMessage.value =
                        UIMessage.SnackBarMessage(resourceManager.getString(R.string.core_error_no_connection))
                } else {
                    _uiMessage.value =
                        UIMessage.SnackBarMessage(resourceManager.getString(R.string.core_error_unknown_error))
                }
            } finally {
                isLoading = false
                _isUpdating.value = false
            }
        }
    }

    fun updateData(
        username: String? = null,
        organization: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isUpdating.value = true
                isLoading = true
                page = 1
                val response = discoveryInteractor.getCoursesList(username, organization, page)
                if (response.pagination.next.isNotEmpty() && page != response.pagination.numPages) {
                    _canLoadMore.value = true
                    page++
                } else {
                    _canLoadMore.value = false
                    page = -1
                }
                coursesList.clear()
                coursesList.addAll(response.results)
                _uiState.value = DiscoveryUIState.Courses(ArrayList(coursesList))
            } catch (e: Exception) {
                if (e.isInternetError()) {
                    _uiMessage.value =
                        UIMessage.SnackBarMessage(resourceManager.getString(R.string.core_error_no_connection))
                } else {
                    _uiMessage.value =
                        UIMessage.SnackBarMessage(resourceManager.getString(R.string.core_error_unknown_error))
                }
            } finally {
                isLoading = false
                _isUpdating.value = false
            }
        }
    }

    fun fetchMore() {
        if (!isLoading && page != -1) {
            loadCoursesInternal()
        }
    }

    fun searchCatalogCourses(
        searchTerm: String? = null,
        selected: Map<String, String> = emptyMap()
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = DiscoveryUIState.Loading
                val category = selected["categories"]?.takeIf { it.isNotBlank() && !it.startsWith("All") }
                val level = selected["levels"]?.takeIf { it.isNotBlank() && !it.startsWith("All") }
                val subject = selected["subjects"]?.takeIf { it.isNotBlank() && !it.startsWith("All") }

                val resp = catalogApi.getCourses(
                    searchTerm = searchTerm?.takeIf { it.isNotBlank() },
                    category = category,
                    level = level,
                    subject = subject
                )
                val mapped = resp.results.map { c ->
                    org.openedx.discovery.domain.model.Course(
                        id = c.id,
                        blocksUrl = "",
                        courseId = c.course_id ?: c.id,
                        effort = "",
                        enrollmentStart = null,
                        enrollmentEnd = null,
                        hidden = c.hidden ?: false,
                        invitationOnly = c.invitation_only ?: false,
                        media = org.openedx.core.domain.model.Media(
                            bannerImage = null,
                            courseImage = null,
                            courseVideo = null,
                            image = org.openedx.core.domain.model.Image(
                                raw = c.media?.image?.raw ?: "",
                                small = c.media?.image?.small ?: "",
                                large = c.media?.image?.large ?: ""
                            )
                        ),
                        mobileAvailable = c.mobile_available ?: true,
                        name = c.name,
                        number = "",
                        org = c.org ?: "",
                        pacing = "",
                        shortDescription = c.short_description ?: "",
                        start = c.start ?: "",
                        end = c.end ?: "",
                        startDisplay = c.start_display ?: "",
                        startType = c.start_type ?: "",
                        overview = "",
                        isEnrolled = false
                    )
                }
                _uiState.value = DiscoveryUIState.Courses(mapped)
            } catch (e: Exception) {
                if (e.isInternetError()) {
                    _uiMessage.value =
                        UIMessage.SnackBarMessage(resourceManager.getString(R.string.core_error_no_connection))
                } else {
                    _uiMessage.value =
                        UIMessage.SnackBarMessage(resourceManager.getString(R.string.core_error_unknown_error))
                }
            }
        }
    }

    fun courseDetailClicked(courseId: String, courseName: String) {
        analytics.discoveryCourseClickedEvent(courseId, courseName)
    }

    fun courseDetailClickedEvent(courseId: String, courseTitle: String) {
        val event = DiscoveryAnalyticsEvent.COURSE_INFO
        analytics.logEvent(
            event.eventName,
            buildMap {
                put(DiscoveryAnalyticsKey.NAME.key, event.biValue)
                put(DiscoveryAnalyticsKey.COURSE_ID.key, courseId)
                put(DiscoveryAnalyticsKey.COURSE_NAME.key, courseTitle)
                put(DiscoveryAnalyticsKey.CATEGORY.key, DiscoveryAnalyticsKey.DISCOVERY.key)
            }
        )
    }

    fun navigateToSignIn(parentFragmentManager: FragmentManager) {
        router.navigateToSignIn(parentFragmentManager, courseId, null)
        logEvent(AuthAnalyticsEvent.SIGN_IN_CLICKED)
    }

    fun navigateToCourseDetail(parentFragmentManager: FragmentManager,courseId: String) {
        router.navigateToCourseDetail(
            parentFragmentManager,
            courseId
        )
    }

    fun signInBrowser(activityContext: Activity) {
        viewModelScope.launch {
            runCatching {
                browserAuthHelper.signIn(activityContext)
            }.onFailure {
                logger.e { "Browser auth error: $it" }
            }
        }
    }

    fun navigateToSignUp(parentFragmentManager: FragmentManager) {
        router.navigateToSignUp(parentFragmentManager, courseId, null)
        logEvent(AuthAnalyticsEvent.REGISTER_CLICKED)
    }

    fun navigateToDiscovery(parentFragmentManager: FragmentManager, querySearch: String) {
        if (discoveryTypeWebView) {
            router.navigateToWebDiscoverCourses(
                parentFragmentManager,
                querySearch
            )
        } else {
            router.navigateToNativeDiscoverCourses(
                parentFragmentManager,
                querySearch
            )
        }
        querySearch.takeIfNotEmpty()?.let {
            logEvent(
                event = AuthAnalyticsEvent.DISCOVERY_COURSES_SEARCH,
                params = buildMap {
                    put(AuthAnalyticsKey.SEARCH_QUERY.key, querySearch)
                }
            )
        } ?: logEvent(event = AuthAnalyticsEvent.EXPLORE_ALL_COURSES)
    }

    private fun logEvent(
        event: AuthAnalyticsEvent,
        params: Map<String, Any?> = emptyMap(),
    ) {
        analytics.logEvent(
            event = event.eventName,
            params = buildMap {
                put(AuthAnalyticsKey.NAME.key, event.biValue)
                putAll(params)
            }
        )
    }

    private fun logLogistrationScreenEvent() {
        val event = AuthAnalyticsEvent.Logistration
        analytics.logScreenEvent(
            screenName = event.eventName,
            params = buildMap {
                put(AuthAnalyticsKey.NAME.key, event.biValue)
            }
        )
    }
}
