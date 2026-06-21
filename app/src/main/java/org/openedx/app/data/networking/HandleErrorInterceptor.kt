package org.openedx.app.data.networking

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
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

        val body = response.body
        val source = body?.source()
        source?.request(Long.MAX_VALUE)
        val buffer = source?.buffer
        val jsonStr = buffer?.clone()?.readString(Charsets.UTF_8)

        return if (jsonStr != null) {
            try {
                handleErrorResponse(response, jsonStr)
            } catch (e: Exception) {
                if (e is EdxError) throw e
                response
            }
        } else {
            response
        }
    }

    private fun isErrorResponse(response: Response): Boolean {
        return response.code in 400..500 && response.body != null
    }

    private fun handleErrorResponse(response: Response, jsonStr: String): Response {
        return try {
            val errorResponse = gson.fromJson(jsonStr, ErrorResponse::class.java)
            handleParsedErrorResponse(errorResponse) ?: response
        } catch (e: JsonSyntaxException) {
            throw IOException("JsonSyntaxException $jsonStr", e)
        }
    }

    private fun handleParsedErrorResponse(errorResponse: ErrorResponse?): Response? {
        val exception = when {
            errorResponse?.error == ERROR_INVALID_GRANT -> EdxError.InvalidGrantException()
            errorResponse?.error == ERROR_USER_NOT_ACTIVE -> EdxError.UserNotActiveException()
            errorResponse?.errorDescription != null ->
                EdxError.ValidationException(errorResponse.errorDescription.orEmpty())

            else -> return null
        }
        throw exception
    }

    companion object {
        const val ERROR_INVALID_GRANT = "invalid_grant"
        const val ERROR_USER_NOT_ACTIVE = "user_not_active"
    }
}
