package org.openedx.core.utils

object Validators {
    private const val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"

    fun isValidPassword(password: String): Boolean {
        return password.matches(PASSWORD_PATTERN.toRegex())
    }
}
