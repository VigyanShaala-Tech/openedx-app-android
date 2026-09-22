package org.openedx.app

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.openedx.auth.presentation.logistration.LogistrationFragment
import org.openedx.core.config.Config
import org.openedx.core.system.notifier.DiscoveryNotifier
import org.openedx.core.system.notifier.NavigationToDiscovery
import org.openedx.core.system.notifier.app.AppNotifier
import org.openedx.core.system.notifier.app.AppUpgradeEvent
import org.openedx.foundation.extension.toImageLink
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.profile.data.repository.ProfileRepository
import org.openedx.profile.domain.model.Account
import org.openedx.profile.system.notifier.account.AccountUpdated
import org.openedx.profile.system.notifier.profile.ProfileNotifier

class MainViewModel(
    private val config: Config,
    private val notifier: DiscoveryNotifier,
    private val analytics: AppAnalytics,
    private val appNotifier: AppNotifier,
    private val profileRepository: ProfileRepository,
    private val profileNotifier: ProfileNotifier,
) : BaseViewModel() {

    private val _isBottomBarEnabled = MutableLiveData(true)
    val isBottomBarEnabled: LiveData<Boolean>
        get() = _isBottomBarEnabled

    private val _navigateToDiscovery = MutableSharedFlow<Boolean>()
    val navigateToDiscovery: SharedFlow<Boolean>
        get() = _navigateToDiscovery.asSharedFlow()

    private val _appUpgradeEvent = MutableLiveData<AppUpgradeEvent>()
    val appUpgradeEvent: LiveData<AppUpgradeEvent>
        get() = _appUpgradeEvent

    private val _profileImageUrl = MutableLiveData<String?>()
    val profileImageUrl: LiveData<String?>
        get() = _profileImageUrl

    val isDiscoveryTypeWebView get() = config.getDiscoveryConfig().isViewTypeWebView()
//    val getDiscoveryFragment get() = DiscoveryNavigator(isDiscoveryTypeWebView).getDiscoveryFragment()
    val getDiscoveryFragment get() = LogistrationFragment.newInstance(null, "RECOMMENDED")

    val isDownloadsFragmentEnabled get() = config.getDownloadsConfig().isEnabled

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        collectDiscoveryEvents()
        collectAppUpgradeEvent()
        collectProfileEvents()
        loadProfileImage()
    }

    fun enableBottomBar(enable: Boolean) {
        _isBottomBarEnabled.value = enable
    }

    fun logLearnTabClickedEvent() {
        logScreenEvent(AppAnalyticsEvent.LEARN)
    }

    fun logDiscoveryTabClickedEvent() {
        logScreenEvent(AppAnalyticsEvent.DISCOVER)
    }

    fun logDownloadsTabClickedEvent() {
        logScreenEvent(AppAnalyticsEvent.DOWNLOADS)
    }

    fun logProfileTabClickedEvent() {
        logScreenEvent(AppAnalyticsEvent.PROFILE)
    }

    private fun logScreenEvent(event: AppAnalyticsEvent) {
        analytics.logScreenEvent(
            screenName = event.eventName,
            params = buildMap {
                put(AppAnalyticsKey.NAME.key, event.biValue)
            }
        )
    }

    fun loadProfileImage() {
        viewModelScope.launch {
            try {
                val cachedAccount = profileRepository.getCachedAccount()
                updateProfileImageUrl(cachedAccount)
                val account = profileRepository.getAccount()
                updateProfileImageUrl(account)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateProfileImageUrl(account: Account?) {
        if (account?.profileImage?.hasImage == true && account.profileImage.imageUrlFull.isNotBlank()) {
            val imageUrl = account.profileImage.imageUrlFull.toImageLink(config.getApiHostURL())
            _profileImageUrl.postValue(imageUrl)
        } else {
            _profileImageUrl.postValue(null)
        }
    }

    private fun collectProfileEvents() {
        viewModelScope.launch {
            profileNotifier.notifier
                .onEach { event ->
                    if (event is AccountUpdated) {
                        loadProfileImage()
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun collectDiscoveryEvents() {
        notifier.notifier
            .onEach {
                if (it is NavigationToDiscovery) {
                    _navigateToDiscovery.emit(true)
                }
            }
            .distinctUntilChanged()
            .launchIn(viewModelScope)
    }

    private fun collectAppUpgradeEvent() {
        viewModelScope.launch {
            appNotifier.notifier
                .onEach { event ->
                    if (event is AppUpgradeEvent) {
                        _appUpgradeEvent.value = event
                    }
                }
                .distinctUntilChanged()
                .launchIn(viewModelScope)
        }
    }
}
