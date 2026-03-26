package org.openedx.core

import java.util.regex.Pattern

class Validator {

    fun isEmailOrUserNameValid(input: String): Boolean {
        return if (input.contains("@")) {
            val validEmailAddressRegex = Pattern.compile(
                "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
                Pattern.CASE_INSENSITIVE
            )
            validEmailAddressRegex.matcher(input).find()
        } else {
            input.isNotBlank() && input.contains(" ").not()
        }
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length >= 2
    }

    fun formatPhoneNumber(phone: String?): String {
        if (phone.isNullOrEmpty()) return ""

        val cleaned = phone.replace(Regex("\\D"), "")

        if (phone.startsWith("+91")) return "+91" + cleaned.removePrefix("91")

        return when {
            cleaned.startsWith("91") && cleaned.length == 12 -> "+$cleaned"
            cleaned.length == 10 -> "+91$cleaned"
            else -> "+91${cleaned.takeLast(10)}"
        }
    }
}
