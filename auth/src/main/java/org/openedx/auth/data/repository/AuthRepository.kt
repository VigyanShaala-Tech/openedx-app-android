package org.openedx.auth.data.repository

import org.openedx.auth.data.api.AuthApi
import org.openedx.auth.data.model.AuthType
import org.openedx.auth.data.model.ValidationFields
import org.openedx.auth.domain.model.AuthResponse
import org.openedx.core.ApiConstants
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.model.RegistrationField
import org.openedx.core.system.EdxError
import org.openedx.auth.data.api.OtpApi
import org.openedx.auth.data.api.OtpLoginRequest
import org.openedx.auth.data.api.OtpSendRequest
import org.openedx.auth.data.api.OtpVerifyRequest
import org.openedx.auth.data.model.VsRegisterRequest

class AuthRepository(
    private val config: Config,
    private val api: AuthApi,
    private val preferencesManager: CorePreferences,
    private val otpApi: OtpApi,
) {

    suspend fun login(
        username: String,
        password: String,
    ) {
        api.getAccessToken(
            ApiConstants.GRANT_TYPE_PASSWORD,
            config.getOAuthClientId(),
            username,
            password,
            config.getAccessTokenType(),
        )
            .mapToDomain()
            .processAuthResponse()
    }

    suspend fun socialLogin(token: String?, authType: AuthType) {
        require(!token.isNullOrBlank()) { "Token is null" }
        api.exchangeAccessToken(
            accessToken = token,
            clientId = config.getOAuthClientId(),
            tokenType = config.getAccessTokenType(),
            authType = authType.postfix
        )
            .mapToDomain()
            .processAuthResponse()
    }

    suspend fun browserAuthCodeLogin(code: String) {
        api.getAccessTokenFromCode(
            grantType = ApiConstants.GRANT_TYPE_CODE,
            clientId = config.getOAuthClientId(),
            code = code,
            redirectUri = "${config.getAppId()}://${ApiConstants.BrowserLogin.REDIRECT_HOST}",
            tokenType = config.getAccessTokenType(),
        ).mapToDomain().processAuthResponse()
    }

    suspend fun getRegistrationFields(): List<RegistrationField> {
        return api.getRegistrationFields().fields?.map { it.mapToDomain() } ?: emptyList()
    }

    suspend fun register(mapFields: Map<String, String>) {
        return api.registerUser(mapFields)
    }

    suspend fun registerVs(body: VsRegisterRequest) {
        return api.registerUserVs(
            email = body.email,
            name = body.name,
            password = body.password,
            phone_number = body.phoneNumber,
            terms_of_service = body.termsOfService,
            user_role = body.userRole,
            username = body.username,
            verification_key = body.verificationKey,
        )
    }

    suspend fun validateRegistrationFields(mapFields: Map<String, String>): ValidationFields {
        return api.validateRegistrationFields(mapFields)
    }

    suspend fun passwordReset(email: String): Boolean {
        return api.passwordReset(email).success
    }

    suspend fun sendOtp(contact: String) = otpApi.send(OtpSendRequest(contact))
    suspend fun verifyOtp(contact: String, otp: String, key: String) =
        otpApi.verify(OtpVerifyRequest(contact, otp, key))
    suspend fun loginWithOtp(contact: String, otp: String, key: String) {
        otpApi.login(OtpLoginRequest(contact, otp, key))
            .mapToDomain()
            .processAuthResponse()
    }

    private suspend fun AuthResponse.processAuthResponse() {
        if (error != null) {
            throw EdxError.UnknownException(error!!)
        }
        preferencesManager.accessToken = accessToken ?: ""
        preferencesManager.refreshToken = refreshToken ?: ""
        preferencesManager.accessTokenExpiresAt = getTokenExpiryTime()
        val user = api.getProfile()
        preferencesManager.user = user
    }
}
