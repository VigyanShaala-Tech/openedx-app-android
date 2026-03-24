package org.openedx.auth.data.api

import org.openedx.auth.data.model.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

data class OtpSendRequest(val contact_identifier: String)
data class OtpVerifyRequest(val contact_identifier: String, val otp_code: String, val verification_key: String)
data class OtpLoginRequest(val contact_identifier: String, val otp_code: String, val verification_key: String)

data class OtpSendResponse(
    val success: Boolean,
    val message: String,
    val verification_key: String?,
    val resend_after_seconds: Int?,
    val expires_in_seconds: Int?
)

data class OtpVerifyResponse(
    val success: Boolean,
    val message: String,
    val verification_key: String?
)

interface OtpApi {
    @POST("/otp/login/send/")
    suspend fun send(@Body body: OtpSendRequest): OtpSendResponse

    @POST("/otp/verify/")
    suspend fun verify(@Body body: OtpVerifyRequest): OtpVerifyResponse

    @POST("/otp/login/")
    suspend fun login(@Body body: OtpLoginRequest): AuthResponse
}
