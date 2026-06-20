package org.openedx.auth.presentation.restore

sealed class ResetPasswordUIState {
    object Loading : ResetPasswordUIState()
    object ValidLink : ResetPasswordUIState()
    object InvalidLink : ResetPasswordUIState()
    object Success : ResetPasswordUIState()
}
