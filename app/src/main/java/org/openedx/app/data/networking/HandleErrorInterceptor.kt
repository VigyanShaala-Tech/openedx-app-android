package org.openedx.app.data.networking

import com.google.gson.Gson
import com.google.gson.JsonElement
import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException
import org.openedx.core.data.model.ErrorResponse
import org.openedx.core.system.EdxError

class HandleErrorInterceptor(
    private val gson: Gson
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!isErrorResponse(response)) {
            return response
        }

        val jsonStr = try {
            response.peekBody(MAX_PEEK_SIZE).string()
        } catch (e: Exception) {
            null
        }

        return if (jsonStr != null) {
            try {
                handleErrorResponse(response, jsonStr)
            } catch (e: Exception) {
                if (e is EdxError) throw e
                throw EdxError.UnknownException("HTTP ${response.code}")
            }
        } else {
            if (response.code == 401) {
                throw EdxError.UnknownException("HTTP 401 Unauthorized")
            }
            throw EdxError.UnknownException("HTTP ${response.code}")
        }
    }

    private fun isErrorResponse(response: Response): Boolean {
        return response.code in 400..599 && response.body != null
    }

    private fun handleErrorResponse(response: Response, jsonStr: String): Response {
        return try {
            val jsonElement = gson.fromJson(jsonStr, JsonElement::class.java)

            // 1. Check for critical system errors first
            if (jsonElement != null && jsonElement.isJsonObject) {
                val obj = jsonElement.asJsonObject
                val errorCode = when {
                    obj.has("error") -> obj.get("error").asString
                    obj.has("error_code") -> obj.get("error_code").asString
                    else -> null
                }

                when (errorCode) {
                    ERROR_INVALID_GRANT -> throw EdxError.InvalidGrantException()
                    ERROR_USER_NOT_ACTIVE -> throw EdxError.UserNotActiveException()
                    ERROR_TOKEN_EXPIRED -> throw EdxError.TokenExpiredException()
                }
            }

            // 2. Extract human-readable message with high priority from the raw JSON
            val extractedMessage = jsonElement?.let { extractMessageFromTree(it) }
            if (extractedMessage != null) {
                throw EdxError.ValidationException(extractedMessage)
            }

            // 3. Fallback to generic ErrorResponse parsing if no descriptive message found
            val errorResponse = try {
                gson.fromJson(jsonStr, ErrorResponse::class.java)
            } catch (e: Exception) {
                null
            }
            handleParsedErrorResponse(errorResponse) ?: response
        } catch (e: Exception) {
            if (e is EdxError) throw e
            throw EdxError.UnknownException("HTTP ${response.code}")
        }
    }

    private fun handleParsedErrorResponse(errorResponse: ErrorResponse?): Response? {
        val exception = when {
            errorResponse?.error == ERROR_INVALID_GRANT -> EdxError.InvalidGrantException()
            errorResponse?.error == ERROR_USER_NOT_ACTIVE -> EdxError.UserNotActiveException()
            errorResponse?.error == ERROR_TOKEN_EXPIRED || errorResponse?.errorDescription == ERROR_JWT_TOKEN_EXPIRED ->
                EdxError.TokenExpiredException()

            errorResponse?.errorDescription != null ->
                EdxError.ValidationException(errorResponse.errorDescription.orEmpty())

            else -> {
                throw EdxError.UnknownException("HTTP Error: ${errorResponse?.error ?: "Unknown"}")
            }
        }
        throw exception
    }

    private fun extractMessageFromTree(jsonTree: JsonElement): String? {
        if (!jsonTree.isJsonObject) return null
        val jsonObject = jsonTree.asJsonObject
        val errorMessage = StringBuilder()

        val keysToTry = listOf("email", "username", "message", "user_message", "error_description")
        for (key in keysToTry) {
            if (jsonObject.has(key)) {
                extractMessage(jsonObject.get(key))?.let {
                    if (errorMessage.isNotEmpty()) errorMessage.append("\n")
                    errorMessage.append(it)
                }
            }
        }

        return if (errorMessage.isNotEmpty()) errorMessage.toString() else null
    }

    private fun extractMessage(element: JsonElement): String? {
        if (element.isJsonArray) {
            val array = element.asJsonArray
            if (array.size() > 0) {
                val first = array.get(0)
                if (first.isJsonObject) {
                    val obj = first.asJsonObject
                    if (obj.has("user_message")) return obj.get("user_message").asString
                    if (obj.has("message")) return obj.get("message").asString
                } else if (first.isJsonPrimitive) {
                    return first.asString
                }
            }
        } else if (element.isJsonObject) {
            val obj = element.asJsonObject
            if (obj.has("user_message")) return obj.get("user_message").asString
            if (obj.has("message")) return obj.get("message").asString
        } else if (element.isJsonPrimitive) {
            return element.asString
        }
        return null
    }

    companion object {
        const val ERROR_INVALID_GRANT = "invalid_grant"
        const val ERROR_USER_NOT_ACTIVE = "user_not_active"
        const val ERROR_TOKEN_EXPIRED = "token_expired"
        const val ERROR_JWT_TOKEN_EXPIRED = "Token has expired."
        private const val MAX_PEEK_SIZE = 1024 * 1024L
    }
}
