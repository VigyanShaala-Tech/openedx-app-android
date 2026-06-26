package org.openedx.profile.presentation.profile

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.R
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager
import org.openedx.profile.domain.interactor.ProfileInteractor
import org.openedx.profile.presentation.edit.WHATSAPP
import org.openedx.profile.presentation.ProfileAnalytics
import org.openedx.profile.presentation.ProfileAnalyticsEvent
import org.openedx.profile.presentation.ProfileAnalyticsKey
import org.openedx.profile.presentation.ProfileRouter
import org.openedx.profile.system.notifier.account.AccountUpdated
import org.openedx.profile.system.notifier.profile.ProfileNotifier

class ProfileViewModel(
    private val interactor: ProfileInteractor,
    private val resourceManager: ResourceManager,
    private val notifier: ProfileNotifier,
    private val analytics: ProfileAnalytics,
    val profileRouter: ProfileRouter
) : BaseViewModel() {

    private val _uiState: MutableStateFlow<ProfileUIState> = MutableStateFlow(ProfileUIState.Loading)
    internal val uiState: StateFlow<ProfileUIState> = _uiState.asStateFlow()

    private val _uiMessage = MutableLiveData<UIMessage>()
    val uiMessage: LiveData<UIMessage>
        get() = _uiMessage

    private val _isUpdating = MutableLiveData<Boolean>()
    val isUpdating: LiveData<Boolean>
        get() = _isUpdating

    private val _isOtpLoading = MutableStateFlow(false)
    val isOtpLoading = _isOtpLoading.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent = _isOtpSent.asStateFlow()

    private val _verificationKey = MutableStateFlow<String?>(null)

    init {
        getAccount()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        viewModelScope.launch {
            notifier.notifier.collect {
                if (it is AccountUpdated) {
                    getAccount()
                }
            }
        }
    }

    private fun getAccount() {
        _uiState.value = ProfileUIState.Loading
        viewModelScope.launch {
            try {
                val cachedAccount = interactor.getCachedAccount()
                if (cachedAccount == null) {
                    _uiState.value = ProfileUIState.Loading
                } else {
                    _uiState.value = ProfileUIState.Data(
                        account = cachedAccount
                    )
                }
                val account = interactor.getAccount()
                _uiState.value = ProfileUIState.Data(
                    account = account
                )
            } catch (e: Exception) {
                if (e.isInternetError()) {
                    _uiMessage.value =
                        UIMessage.SnackBarMessage(resourceManager.getString(R.string.core_error_no_connection))
                } else {
                    _uiMessage.value =
                        UIMessage.SnackBarMessage(resourceManager.getString(R.string.core_error_unknown_error))
                }
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun updateAccount() {
        _isUpdating.value = true
        getAccount()
    }

    fun sendOtp(phoneNumber: String) {
        if (phoneNumber.isBlank()) return
        _isOtpLoading.value = true
        viewModelScope.launch {
            try {
                val response = interactor.sendWhatsappOtp(phoneNumber)
                _verificationKey.value = response.verification_key
                _isOtpSent.value = true
                _uiMessage.value = UIMessage.SnackBarMessage(response.message)
            } catch (e: Exception) {
                _uiMessage.value = UIMessage.SnackBarMessage(e.message ?: "Failed to send OTP")
            } finally {
                _isOtpLoading.value = false
            }
        }
    }

    fun verifyOtp(phoneNumber: String, otp: String) {
        val key = _verificationKey.value ?: return
        _isOtpLoading.value = true
        viewModelScope.launch {
            try {
                val response = interactor.verifyWhatsappOtp(phoneNumber, otp, key)
                if (response.success) {
                    // Update account with verified WhatsApp number
                    interactor.updateAccount(mapOf(
                        WHATSAPP to phoneNumber,
                        "is_whatsapp_verified" to true
                    ))
                    getAccount()
                    _isOtpSent.value = false
                    _verificationKey.value = null
                }
                _uiMessage.value = UIMessage.SnackBarMessage(response.message)
            } catch (e: Exception) {
                _uiMessage.value = UIMessage.SnackBarMessage(e.message ?: "Failed to verify OTP")
            } finally {
                _isOtpLoading.value = false
            }
        }
    }

    fun profileEditClicked(fragmentManager: FragmentManager) {
        (uiState.value as? ProfileUIState.Data)?.let { data ->
            profileRouter.navigateToEditProfile(
                fragmentManager,
                data.account
            )
        }
        logProfileEvent(ProfileAnalyticsEvent.EDIT_CLICKED)
    }

    private fun logProfileEvent(
        event: ProfileAnalyticsEvent,
        params: Map<String, Any?> = emptyMap(),
    ) {
        analytics.logEvent(
            event = event.eventName,
            params = buildMap {
                put(ProfileAnalyticsKey.NAME.key, event.biValue)
                put(ProfileAnalyticsKey.CATEGORY.key, ProfileAnalyticsKey.PROFILE.key)
                putAll(params)
            }
        )
    }
}
