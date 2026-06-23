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
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.system.notifier.app.AppNotifier
import org.openedx.core.system.notifier.app.SignInEvent
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager
import java.util.Locale
import org.openedx.core.R as coreR

class VsSignUpViewModel(
    private val interactor: AuthInteractor,
    private val resourceManager: ResourceManager,
    private val analytics: AuthAnalytics,
    private val preferencesManager: CorePreferences,
    private val appNotifier: AppNotifier,
    private val router: AuthRouter,
    private val config: Config,
    val courseId: String?,
    val infoType: String?,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(VsSignUpUIState(
        tosUrl = config.getAgreement(Locale.getDefault().language).tosUrl
    ))
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
        userRole: String,
        socialAuth: SocialAuthResponse? = null
    ) {
        _uiState.update { it.copy(isButtonLoading = true) }
        viewModelScope.launch {
            try {
                val body = VsRegisterRequest(
                    email = email,
                    name = name,
                    password = password,
                    phoneNumber = null,
                    termsOfService = true,
                    userRole = userRole.takeIf { it.isNotBlank() },
                    username = email,
                    verificationKey = null
                )

                interactor.registerVs(body)

                if (socialAuth != null) {
                    interactor.loginSocial(socialAuth.accessToken, socialAuth.authType)
                    setUserId()
                    _uiState.update { it.copy(successLogin = true, isButtonLoading = false) }
                    appNotifier.send(SignInEvent())
                } else {
                    _uiState.update {
                        it.copy(
                            showRegisterSuccessDialog = true,
                            isButtonLoading = false
                        )
                    }
                }
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

    private fun setUserId() {
        preferencesManager.user?.let {
            analytics.setUserIdForSession(it.id)
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
    val showRegisterSuccessDialog: Boolean = false,
    val navigateToSignIn: Boolean = false,
    val socialAuth: SocialAuthResponse? = null,
    val tosUrl: String = "",
)
