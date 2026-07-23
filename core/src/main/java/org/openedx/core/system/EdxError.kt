package org.openedx.core.system

import java.io.IOException

sealed class EdxError(message: String? = null) : IOException(message) {
    class InvalidGrantException : EdxError()
    class UserNotActiveException : EdxError()
    class TokenExpiredException : EdxError()
    class ValidationException(val error: String) : EdxError(error)
    data class UnknownException(val error: String) : EdxError(error)
}
