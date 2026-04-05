package org.openedx.auth.presentation.signup

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openedx.auth.data.model.VsRegisterRequest
import org.openedx.auth.domain.interactor.AuthInteractor
import org.openedx.auth.domain.model.SocialAuthResponse
import org.openedx.auth.presentation.AuthAnalytics
import org.openedx.auth.presentation.AuthRouter
import org.openedx.core.Validator
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.system.notifier.app.AppNotifier
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

    fun showValidationMessage(message: String) {
        viewModelScope.launch {
            _uiMessage.emit(UIMessage.SnackBarMessage(message))
        }
    }

    fun register(
        email: String,
        name: String,
        password: String,
        phoneNumber: String,
        userRole: String,
        verificationKey: String? = null
    ) {
        _uiState.update { it.copy(isButtonLoading = true) }
        viewModelScope.launch {
            try {
                val body = VsRegisterRequest(
                    email = email,
                    name = name,
                    password = password,
                    phoneNumber = Validator().formatPhoneNumber(phoneNumber),
                    termsOfService = true,
                    userRole = userRole.takeIf { it.isNotBlank() },
                    username = email,
                    verificationKey = verificationKey ?: uiState.value.verificationKey.takeIf { uiState.value.isOtpVerified }
                )

                interactor.registerVs(body)

                _uiState.update { it.copy(showRegisterSuccessDialog = true, isButtonLoading = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isButtonLoading = false) }
                val errorMessage = if (e.isInternetError()) {
                    resourceManager.getString(coreR.string.core_error_no_connection)
                } else {
                    e.message ?: resourceManager.getString(coreR.string.core_error_unknown_error)
                }
                _uiMessage.emit(UIMessage.SnackBarMessage(errorMessage))
            }
        }
    }

    fun sendOtp(phoneNumber: String) {
        if (phoneNumber.isBlank()) return
        _uiState.update { it.copy(isOtpLoading = true) }
        viewModelScope.launch {
            try {
                val formattedPhone = Validator().formatPhoneNumber(phoneNumber)
                val response = if (uiState.value.isOtpSent) {
                    interactor.resendSignUpOtp(formattedPhone)
                } else {
                    interactor.sendSignUpOtp(formattedPhone)
                }

                val resendAfter = response.resend_after_seconds ?: 30
                _uiState.update {
                    it.copy(
                        isOtpSent = true,
                        verificationKey = response.verification_key,
                        isOtpLoading = false
                    )
                }
                _uiMessage.emit(UIMessage.SnackBarMessage(response.message))
            } catch (e: Exception) {
                _uiState.update { it.copy(isOtpLoading = false) }
                val errorMessage = if (e.isInternetError()) {
                    resourceManager.getString(coreR.string.core_error_no_connection)
                } else {
                    e.message ?: "Failed to send OTP"
                }
                _uiMessage.emit(UIMessage.SnackBarMessage(errorMessage))
            }
        }
    }


    fun verifyOtp(phoneNumber: String, otpCode: String) {
        val verificationKey = uiState.value.verificationKey ?: return
        _uiState.update { it.copy(isOtpLoading = true) }
        viewModelScope.launch {
            try {
                val response = interactor.verifyOtp(Validator().formatPhoneNumber(phoneNumber), otpCode, verificationKey)
                _uiState.update {
                    it.copy(
                        isOtpVerified = true,
                        verificationKey = response.verification_key,
                        isOtpLoading = false
                    )
                }
                _uiMessage.emit(UIMessage.SnackBarMessage(response.message))
            } catch (e: Exception) {
                _uiState.update { it.copy(isOtpLoading = false) }
                val errorMessage = if (e.isInternetError()) {
                    resourceManager.getString(coreR.string.core_error_no_connection)
                } else {
                    e.message ?: "Failed to verify OTP"
                }
                _uiMessage.emit(UIMessage.SnackBarMessage(errorMessage))
            }
        }
    }

    fun navigateToSignIn() {
        _uiState.update { it.copy(navigateToSignIn = true) }
    }

    fun setSocialAuth(socialAuth: SocialAuthResponse) {
        _uiState.update { it.copy(socialAuth = socialAuth) }
    }
}

data class VsSignUpUIState(
    val isButtonLoading: Boolean = false,
    val successLogin: Boolean = false,
    val isOtpLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val isOtpVerified: Boolean = false,
    val verificationKey: String? = null,
    val showRegisterSuccessDialog: Boolean = false,
    val navigateToSignIn: Boolean = false,
    val socialAuth: SocialAuthResponse? = null,
)
