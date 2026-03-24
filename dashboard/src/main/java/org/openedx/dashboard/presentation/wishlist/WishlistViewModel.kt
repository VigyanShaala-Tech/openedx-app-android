package org.openedx.dashboard.presentation.wishlist

import androidx.fragment.app.FragmentManager
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
import org.openedx.dashboard.data.model.CourseItemDto
import org.openedx.dashboard.domain.interactor.DashboardInteractor
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager

data class WishlistUIState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val items: List<CourseItemDto> = emptyList(),
)

class WishlistViewModel(
    private val config: Config,
    private val networkConnection: NetworkConnection,
    private val interactor: DashboardInteractor,
    private val resourceManager: ResourceManager,
) : BaseViewModel() {

    val apiHostUrl get() = config.getApiHostURL()
    val hasInternetConnection: Boolean get() = networkConnection.isOnline()

    private val _uiState = MutableStateFlow(WishlistUIState())
    val uiState: StateFlow<WishlistUIState> get() = _uiState.asStateFlow()

    private val _uiMessage = MutableSharedFlow<UIMessage>()
    val uiMessage: SharedFlow<UIMessage> get() = _uiMessage.asSharedFlow()

    init {
        loadWishlist(isRefreshing = false)
    }

    fun refresh() {
        loadWishlist(isRefreshing = true)
    }

    private fun loadWishlist(isRefreshing: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = !isRefreshing, refreshing = isRefreshing)
            try {
                val response = interactor.getWishlist()
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    refreshing = false,
                    items = response.results
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
