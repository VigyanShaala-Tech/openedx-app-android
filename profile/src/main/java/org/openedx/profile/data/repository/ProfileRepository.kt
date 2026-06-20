package org.openedx.profile.data.repository

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.openedx.core.ApiConstants
import org.openedx.core.DatabaseManager
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.profile.data.api.ProfileApi
import org.openedx.profile.data.storage.ProfilePreferences
import org.openedx.profile.domain.model.Account
import java.io.File

class ProfileRepository(
    private val config: Config,
    private val api: ProfileApi,
    private val profilePreferences: ProfilePreferences,
    private val corePreferences: CorePreferences,
    private val databaseManager: DatabaseManager
) {

    suspend fun getAccount(): Account {
        val account = api.getAccount(corePreferences.user?.username!!)
        profilePreferences.profile = account
        return account.mapToDomain()
    }

    suspend fun getAccount(username: String): Account {
        val account = api.getAccount(username)
        return account.mapToDomain()
    }

    fun getCachedAccount(): Account? {
        return profilePreferences.profile?.mapToDomain()
    }

    suspend fun updateAccount(fields: Map<String, Any?>): Account {
        return api.updateAccount(corePreferences.user?.username!!, fields).mapToDomain()
    }

    suspend fun setProfileImage(file: File, mimeType: String) {
        api.setProfileImage(
            corePreferences.user?.username!!,
            "attachment;filename=filename.${file.extension}",
            true,
            file.asRequestBody(mimeType.toMediaType())
        )
    }

    suspend fun deleteProfileImage() {
        api.deleteProfileImage(corePreferences.user?.username!!)
    }

    suspend fun deactivateAccount(password: String) = api.deactivateAccount(password)

    suspend fun sendWhatsappOtp(phoneNumber: String): org.openedx.profile.data.model.OtpSendResponse {
        val body = mapOf("contact_identifier" to phoneNumber)
        val response = api.sendWhatsappOtp(body)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response")
        } else {
            throw Exception(response.errorBody()?.string() ?: "Failed to send OTP")
        }
    }

    suspend fun verifyWhatsappOtp(phoneNumber: String, otp: String, verificationKey: String): org.openedx.profile.data.model.OtpVerifyResponse {
        val body = mapOf(
            "contact_identifier" to phoneNumber,
            "otp_code" to otp,
            "verification_key" to verificationKey
        )
        val response = api.verifyWhatsappOtp(body)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response")
        } else {
            throw Exception(response.errorBody()?.string() ?: "Failed to verify OTP")
        }
    }

    suspend fun logout() {
        try {
            api.revokeAccessToken(
                config.getOAuthClientId(),
                corePreferences.refreshToken,
                ApiConstants.TOKEN_TYPE_REFRESH
            )
        } finally {
            corePreferences.clearCorePreferences()
            databaseManager.clearTables()
        }
    }
}
