package org.openedx.auth.presentation.signup

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openedx.auth.domain.interactor.AuthInteractor
import org.openedx.auth.presentation.AuthAnalytics
import org.openedx.auth.presentation.AuthRouter
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.system.notifier.app.AppNotifier
import org.openedx.core.system.notifier.app.SignInEvent
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager
import org.openedx.core.R as coreR

class VsSignUpViewModel(
    private val interactor: AuthInteractor,
    private val resourceManager: ResourceManager,
    private val analytics: AuthAnalytics,
    private val preferencesManager: CorePreferences,
    private val appNotifier: AppNotifier,
    private val router: AuthRouter,
    val courseId: String?,
    val infoType: String?,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(VsSignUpUIState())
    val uiState = _uiState.asStateFlow()

    private val _uiMessage = MutableSharedFlow<UIMessage>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val uiMessage = _uiMessage.asSharedFlow()

    fun register(
        email: String,
        name: String,
        password: String,
        phoneNumber: String,
        userRole: String
    ) {
        _uiState.update { it.copy(isButtonLoading = true) }
        viewModelScope.launch {
            try {
                val payload = mutableMapOf<String, Any>(
                    "email" to email,
                    "name" to name,
                    "password" to password,
                    "phone_number" to phoneNumber,
                    "terms_of_service" to true,
                    "user_role" to userRole,
                    "username" to email
                )
                if (uiState.value.isOtpVerified) {
                    uiState.value.verificationKey?.let {
                        payload["verification_key"] = it
                    }
                }

                interactor.registerVs(payload)
                
                // After successful registration, login the user
                interactor.login(email, password)
                
                _uiState.update { it.copy(successLogin = true, isButtonLoading = false) }
                appNotifier.send(SignInEvent())
            } catch (e: Exception) {
                _uiState.update { it.copy(isButtonLoading = false) }
                val errorMessage = if (e.isInternetError()) {
                    coreR.string.core_error_no_connection
                } else {
                    coreR.string.core_error_unknown_error
                }
                _uiMessage.emit(UIMessage.SnackBarMessage(resourceManager.getString(errorMessage)))
            }
        }
    }

    fun sendOtp(phoneNumber: String) {
        if (phoneNumber.isBlank()) return
        _uiState.update { it.copy(isOtpLoading = true) }
        viewModelScope.launch {
            try {
                val response = interactor.sendOtp(phoneNumber)
                if (response.success) {
                    _uiState.update {
                        it.copy(
                            isOtpSent = true,
                            verificationKey = response.verification_key,
                            isOtpLoading = false
                        )
                    }
                    _uiMessage.emit(UIMessage.SnackBarMessage(response.message))
                } else {
                    _uiState.update { it.copy(isOtpLoading = false) }
                    _uiMessage.emit(UIMessage.SnackBarMessage(response.message))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isOtpLoading = false) }
                _uiMessage.emit(UIMessage.SnackBarMessage(e.message ?: "Failed to send OTP"))
            }
        }
    }

    fun verifyOtp(phoneNumber: String, otpCode: String) {
        val verificationKey = uiState.value.verificationKey ?: return
        _uiState.update { it.copy(isOtpLoading = true) }
        viewModelScope.launch {
            try {
                val response = interactor.verifyOtp(phoneNumber, otpCode, verificationKey)
                if (response.success) {
                    _uiState.update {
                        it.copy(
                            isOtpVerified = true,
                            verificationKey = response.verification_key,
                            isOtpLoading = false
                        )
                    }
                    _uiMessage.emit(UIMessage.SnackBarMessage(response.message))
                } else {
                    _uiState.update { it.copy(isOtpLoading = false) }
                    _uiMessage.emit(UIMessage.SnackBarMessage(response.message))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isOtpLoading = false) }
                _uiMessage.emit(UIMessage.SnackBarMessage(e.message ?: "Failed to verify OTP"))
            }
        }
    }
}

data class VsSignUpUIState(
    val isButtonLoading: Boolean = false,
    val successLogin: Boolean = false,
    val isOtpLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val isOtpVerified: Boolean = false,
    val verificationKey: String? = null
)
