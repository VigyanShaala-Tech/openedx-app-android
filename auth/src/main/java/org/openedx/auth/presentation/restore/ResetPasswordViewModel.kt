package org.openedx.auth.presentation.restore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.openedx.auth.domain.interactor.AuthInteractor
import org.openedx.auth.presentation.AuthAnalytics
import org.openedx.core.system.notifier.app.AppNotifier
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.SingleEventLiveData
import org.openedx.foundation.presentation.UIMessage

class ResetPasswordViewModel(
    private val authInteractor: AuthInteractor,
    private val appNotifier: AppNotifier,
    private val analytics: AuthAnalytics
) : BaseViewModel() {

    private val _uiState = MutableLiveData<ResetPasswordUIState>(ResetPasswordUIState.Loading)
    val uiState: LiveData<ResetPasswordUIState>
        get() = _uiState

    private val _uiMessage = SingleEventLiveData<UIMessage>()
    val uiMessage: LiveData<UIMessage>
        get() = _uiMessage

    private val _isButtonLoading = MutableLiveData<Boolean>(false)
    val isButtonLoading: LiveData<Boolean>
        get() = _isButtonLoading

    fun validateToken(token: String) {
        _uiState.value = ResetPasswordUIState.Loading
        viewModelScope.launch {
            try {
                val isValid = authInteractor.validatePasswordResetToken(token)
                if (isValid) {
                    _uiState.value = ResetPasswordUIState.ValidLink
                } else {
                    _uiState.value = ResetPasswordUIState.InvalidLink
                }
            } catch (e: Exception) {
                _uiState.value = ResetPasswordUIState.InvalidLink
            }
        }
    }

    fun resetPassword(token: String, password1: String, password2: String) {
        if (password1 != password2) {
            _uiMessage.value = UIMessage.SnackBarMessage("Passwords do not match")
            return
        }
        
        _isButtonLoading.value = true
        viewModelScope.launch {
            try {
                // First validate the password format
                val validation = authInteractor.validatePassword(password1)
                val passwordError = validation.validationResult["password"]
                if (!passwordError.isNullOrEmpty()) {
                    _uiMessage.value = UIMessage.SnackBarMessage(passwordError)
                    _isButtonLoading.value = false
                    return@launch
                }

                // Then confirm reset
                authInteractor.resetPasswordConfirm(token, password1, password2)
                _uiState.value = ResetPasswordUIState.Success
            } catch (e: Exception) {
                _uiMessage.value = UIMessage.SnackBarMessage(e.message ?: "Failed to reset password")
            } finally {
                _isButtonLoading.value = false
            }
        }
    }
}
